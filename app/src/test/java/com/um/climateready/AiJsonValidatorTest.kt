package com.um.climateready

import com.um.climateready.feature.quiz.AiJsonValidator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AiJsonValidatorTest {

    private val validJson = """
        {
            "question": "What should you do during flash flood warnings around Pantai River near UM?",
            "options": {
                "A": "Park vehicles at basement levels",
                "B": "Move to higher ground and avoid low-lying roads",
                "C": "Walk through flowing floodwater",
                "D": "Wait in underground pedestrian tunnels"
            },
            "correctAnswer": "B",
            "explanation": "Moving to higher ground and avoiding rapidly rising streams prevents stranding and drowning."
        }
    """.trimIndent()

    @Test
    fun testValidJson_passesValidation() {
        assertTrue("Valid JSON should pass validation", AiJsonValidator.validate(validJson))
    }

    @Test
    fun testAllValidAnswerChoicesPass() {
        for (choice in listOf("A", "B", "C", "D")) {
            val json = validJson.replace("\"correctAnswer\": \"B\"", "\"correctAnswer\": \"$choice\"")
            assertTrue("Choice $choice should be accepted as valid correctAnswer", AiJsonValidator.validate(json))
        }
    }

    @Test
    fun testMalformedJson_returnsFalse() {
        val malformed = "{ \"question\": \"Unfinished JSON"
        assertFalse("Malformed JSON syntax must be rejected", AiJsonValidator.validate(malformed))
    }

    @Test
    fun testEmptyAndBlankStrings_returnFalse() {
        assertFalse("Empty string must be rejected", AiJsonValidator.validate(""))
        assertFalse("Blank whitespace must be rejected", AiJsonValidator.validate("   "))
        assertFalse("Plain non-JSON text must be rejected", AiJsonValidator.validate("Hello World"))
    }

    @Test
    fun testMissingQuestion_returnsFalse() {
        val missingQuestion = """
            {
                "options": {
                    "A": "Option A", "B": "Option B", "C": "Option C", "D": "Option D"
                },
                "correctAnswer": "A",
                "explanation": "Valid explanation"
            }
        """.trimIndent()
        assertFalse("JSON missing question field must return false", AiJsonValidator.validate(missingQuestion))

        val blankQuestion = validJson.replace(
            "\"question\": \"What should you do during flash flood warnings around Pantai River near UM?\"",
            "\"question\": \"   \""
        )
        assertFalse("JSON with blank question text must return false", AiJsonValidator.validate(blankQuestion))
    }

    @Test
    fun testMissingExplanation_returnsFalse() {
        val missingExplanation = """
            {
                "question": "Valid Question?",
                "options": {
                    "A": "Option A", "B": "Option B", "C": "Option C", "D": "Option D"
                },
                "correctAnswer": "A"
            }
        """.trimIndent()
        assertFalse("JSON missing explanation must return false", AiJsonValidator.validate(missingExplanation))

        val blankExplanation = validJson.replace(
            "Moving to higher ground and avoiding rapidly rising streams prevents stranding and drowning.",
            "   "
        )
        assertFalse("JSON with blank explanation must return false", AiJsonValidator.validate(blankExplanation))
    }

    @Test
    fun testMissingOptionsObject_returnsFalse() {
        val missingOptions = """
            {
                "question": "Valid Question?",
                "correctAnswer": "A",
                "explanation": "Valid explanation"
            }
        """.trimIndent()
        assertFalse("JSON missing options object must return false", AiJsonValidator.validate(missingOptions))
    }

    @Test
    fun testOptionsMissingChoice_returnsFalse() {
        val onlyThreeOptions = """
            {
                "question": "Valid Question?",
                "options": {
                    "A": "Option A", "B": "Option B", "C": "Option C"
                },
                "correctAnswer": "A",
                "explanation": "Valid explanation"
            }
        """.trimIndent()
        assertFalse("Options with fewer than 4 choices must return false", AiJsonValidator.validate(onlyThreeOptions))
    }

    @Test
    fun testOptionWithBlankValue_returnsFalse() {
        val blankOption = """
            {
                "question": "Valid Question?",
                "options": {
                    "A": "Option A", "B": "Option B", "C": "  ", "D": "Option D"
                },
                "correctAnswer": "A",
                "explanation": "Valid explanation"
            }
        """.trimIndent()
        assertFalse("Options with blank text must return false", AiJsonValidator.validate(blankOption))
    }

    @Test
    fun testInvalidCorrectAnswer_returnsFalse() {
        val invalidChoice = validJson.replace("\"correctAnswer\": \"B\"", "\"correctAnswer\": \"E\"")
        assertFalse("Choice 'E' outside A-D must return false", AiJsonValidator.validate(invalidChoice))

        val lowerCaseChoice = validJson.replace("\"correctAnswer\": \"B\"", "\"correctAnswer\": \"b\"")
        assertFalse("Lowercase choice 'b' must return false", AiJsonValidator.validate(lowerCaseChoice))

        val emptyChoice = validJson.replace("\"correctAnswer\": \"B\"", "\"correctAnswer\": \"\"")
        assertFalse("Empty correct answer must return false", AiJsonValidator.validate(emptyChoice))
    }
}
