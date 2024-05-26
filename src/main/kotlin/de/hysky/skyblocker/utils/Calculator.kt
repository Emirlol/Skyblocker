package de.hysky.skyblocker.utils

import java.util.*
import java.util.regex.Pattern

object Calculator {
	private val NUMBER_PATTERN: Pattern = Pattern.compile("(\\d+\\.?\\d*)([sekmbt]?)")
	private val MAGNITUDE_VALUES: Map<String, Long> = java.util.Map.of(
		"s", 64L,
		"e", 160L,
		"k", 1000L,
		"m", 1000000L,
		"b", 1000000000L,
		"t", 1000000000000L
	)

	private fun lex(input: String): List<Token> {
		var input = input
		val tokens: MutableList<Token> = ArrayList()
		input = input.replace(" ", "").lowercase(Locale.getDefault()).replace("x", "*")
		var i = 0
		while (i < input.length) {
			val token = Token()
			when (input[i]) {
				'+', '-', '*', '/' -> {
					token.type = TokenType.OPERATOR
					token.value = input[i].toString()
					token.tokenLength = 1
				}

				'(' -> {
					token.type = TokenType.L_PARENTHESIS
					token.value = input[i].toString()
					token.tokenLength = 1
					//add implicit multiplication when there is a number before brackets
					if (!tokens.isEmpty()) {
						val lastType = tokens.last.type
						if (lastType == TokenType.R_PARENTHESIS || lastType == TokenType.NUMBER) {
							val mutliplyToken = Token()
							mutliplyToken.type = TokenType.OPERATOR
							mutliplyToken.value = "*"
							tokens.add(mutliplyToken)
						}
					}
				}

				')' -> {
					token.type = TokenType.R_PARENTHESIS
					token.value = input[i].toString()
					token.tokenLength = 1
				}

				else -> {
					token.type = TokenType.NUMBER
					val numberMatcher = NUMBER_PATTERN.matcher(input.substring(i))
					if (!numberMatcher.find()) { //invalid value to lex
						throw UnsupportedOperationException("invalid character")
					}
					val end = numberMatcher.end()
					token.value = input.substring(i, i + end)
					token.tokenLength = end
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
					while (!operatorStack.isEmpty()) {
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
						if (operatorStack.isEmpty()) {
							throw UnsupportedOperationException("Unbalanced left parenthesis")
						}
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
		while (!operatorStack.isEmpty()) {
			val leftToken = operatorStack.pop()
			if (leftToken.type == TokenType.L_PARENTHESIS) {
				//technically unbalanced left parenthesis error but just assume they are close after the equation and ignore them from here
				continue
			}
			outputQueue.add(leftToken)
		}

		return outputQueue.stream().toList()
	}

	private fun getPrecedence(operator: String?): Int {
		return when (operator) {
			"+", "-" -> {
				0
			}

			"*", "/" -> {
				1
			}

			else -> throw UnsupportedOperationException("Invalid operator")
		}
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

	private fun calculateValue(value: String?): Double {
		val numberMatcher = NUMBER_PATTERN.matcher(value!!.lowercase(Locale.getDefault()))
		if (!numberMatcher.matches()) {
			throw UnsupportedOperationException("Invalid number")
		}
		var number = numberMatcher.group(1).toDouble()
		val magnitude = numberMatcher.group(2)

		if (!magnitude.isEmpty()) {
			if (!MAGNITUDE_VALUES.containsKey(magnitude)) { //its invalid if its another letter
				throw UnsupportedOperationException("Invalid magnitude")
			}
			number *= MAGNITUDE_VALUES[magnitude]!!.toDouble()
		}

		return number
	}

	@JvmStatic
    fun calculate(equation: String): Double {
		//custom bit for replacing purse with its value
		var equation = equation
		equation = equation.lowercase(Locale.getDefault()).replace("p(urse)?".toRegex(), Utils.getPurse().toLong().toString())
		return evaluate(shunt(lex(equation)))
	}

	enum class TokenType {
		NUMBER, OPERATOR, L_PARENTHESIS, R_PARENTHESIS
	}

	class Token {
		var type: TokenType? = null
		var value: String? = null
		var tokenLength: Int = 0
	}
}
