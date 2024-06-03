package de.hysky.skyblocker.utils

import java.util.*
import java.util.regex.Pattern

object Calculator {
	private val NUMBER_PATTERN: Pattern = Pattern.compile("(\\d+\\.?\\d*)([sekmbt]?)")
	private val MAGNITUDE_VALUES = mapOf(
		"s" to 64L,
		"e" to 160L,
		"k" to 1000L,
		"m" to 1000000L,
		"b" to 1000000000L,
		"t" to 1000000000000L
	)

	private fun lex(input: String): List<Token> {
		val tokens: MutableList<Token> = ArrayList()
		val cleanInput = input.replace(" ", "").lowercase().replace("x", "*")
		var i = 0
		while (i < cleanInput.length) {
			lateinit var token: Token //`check` blocks leading to Nothing isn't recognized by the compiler for some reason, so this has to be lateinit even though there is no way for it to not be initialized
			when (cleanInput[i]) {
				'+', '-', '*', '/' -> token = Token(TokenType.OPERATOR, cleanInput[i].toString(), 1)

				'(' -> {
					Token(TokenType.L_PARENTHESIS, cleanInput[i].toString(), 1)
					//add implicit multiplication when there is a number before brackets
					if (tokens.isNotEmpty()) {
						val lastType = tokens.last().type
						if (lastType == TokenType.R_PARENTHESIS || lastType == TokenType.NUMBER) {
							tokens.add(Token(TokenType.OPERATOR, "*", 0))
						}
					}
				}

				')' -> token = Token(TokenType.R_PARENTHESIS, cleanInput[i].toString(), 1)

				else -> {
					val numberMatcher = NUMBER_PATTERN.matcher(cleanInput.substring(i))
					check(numberMatcher.find()) { "Invalid character" } //invalid value to lex
					val end = numberMatcher.end()
					token = Token(TokenType.NUMBER, cleanInput.substring(i, i + end), end)
				}
			}
			tokens.add(token)

			i += token.tokenLength
		}

		return tokens
	}

	/**
	 * This is an implementation of the shunting yard algorithm to convert the equation to reverse polish notation
	 *
	 * @param tokens equation in infix notation order
	 * @return equation in RPN order
	 */
	private fun shunt(tokens: List<Token>): List<Token> {
		val operatorStack: Deque<Token> = ArrayDeque()
		val outputQueue: MutableList<Token> = ArrayList()

		for (shuntingToken in tokens) {
			when (shuntingToken.type) {
				TokenType.NUMBER -> outputQueue.add(shuntingToken)
				TokenType.OPERATOR -> {
					val precedence = getPrecedence(shuntingToken.value)
					while (operatorStack.isNotEmpty()) {
						val leftToken = operatorStack.peek()
						if (leftToken.type == TokenType.L_PARENTHESIS) {
							break
						}
						assert(leftToken.type == TokenType.OPERATOR)
						val leftPrecedence = getPrecedence(leftToken.value)
						if (leftPrecedence >= precedence) {
							outputQueue.add(operatorStack.pop())
							continue
						}
						break
					}
					operatorStack.push(shuntingToken)
				}

				TokenType.L_PARENTHESIS -> operatorStack.push(shuntingToken)
				TokenType.R_PARENTHESIS -> {
					while (true) {
						check(operatorStack.isEmpty()) { "Unbalanced left parenthesis" }
						val leftToken = operatorStack.pop()
						if (leftToken.type == TokenType.L_PARENTHESIS) {
							break
						}
						outputQueue.add(leftToken)
					}
				}
			}
		}
		//empty the operator stack
		while (operatorStack.isNotEmpty()) {
			val leftToken = operatorStack.pop()
			if (leftToken.type == TokenType.L_PARENTHESIS) {
				//technically unbalanced left parenthesis error but just assume they are close after the equation and ignore them from here
				continue
			}
			outputQueue.add(leftToken)
		}

		return outputQueue.stream().toList()
	}

	private fun getPrecedence(operator: String?) = when (operator) {
		"+", "-" -> 0
		"*", "/" -> 1
		else -> error("Invalid operator")
	}

	/**
	 * @param tokens list of Tokens in reverse polish notation
	 * @return answer to equation
	 */
	private fun evaluate(tokens: List<Token>): Double {
		val values: Deque<Double> = ArrayDeque()
		for (token in tokens) {
			when (token.type) {
				TokenType.NUMBER -> values.push(calculateValue(token.value))
				TokenType.OPERATOR -> {
					val right = values.pop()
					val left = values.pop()
					when (token.value) {
						"+" -> values.push(left + right)
						"-" -> values.push(left - right)
						"/" -> {
							if (right == 0.0) {
								throw UnsupportedOperationException("Can not divide by 0")
							}
							values.push(left / right)
						}

						"*" -> values.push(left * right)
					}
				}

				TokenType.L_PARENTHESIS, TokenType.R_PARENTHESIS -> throw UnsupportedOperationException("Equation is not in RPN")
			}
		}
		if (values.isEmpty()) {
			throw UnsupportedOperationException("Equation is empty")
		}
		return values.pop()
	}

	private fun calculateValue(value: String): Double {
		val numberMatcher = NUMBER_PATTERN.matcher(value.lowercase(Locale.getDefault()))
		check(numberMatcher.matches()) { "Invalid number" }
		var number = numberMatcher.group(1).toDouble()
		val magnitude = numberMatcher.group(2)

		if (magnitude.isNotEmpty()) {
			check(MAGNITUDE_VALUES.containsKey(magnitude)) { "Invalid magnitude" } //It's invalid if it's another letter

			number *= MAGNITUDE_VALUES[magnitude]!!.toDouble()
		}

		return number
	}

	fun calculate(equation: String): Double {
		//custom bit for replacing purse with its value
		return evaluate(shunt(lex(equation.lowercase(Locale.getDefault()).replace("p(urse)?".toRegex(), Utils.purse.toLong().toString()))))
	}

	enum class TokenType {
		NUMBER, OPERATOR, L_PARENTHESIS, R_PARENTHESIS
	}

	data class Token(val type: TokenType, val value: String, val tokenLength: Int)
}
