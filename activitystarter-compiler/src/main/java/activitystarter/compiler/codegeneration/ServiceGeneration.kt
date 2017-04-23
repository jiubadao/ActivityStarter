package activitystarter.compiler.codegeneration

import activitystarter.compiler.model.classbinding.ClassBinding
import activitystarter.compiler.model.param.ArgumentBinding
import com.squareup.javapoet.MethodSpec

internal class ServiceGeneration(classBinding: ClassBinding) : IntentBinding(classBinding) {

    override fun createFillFieldsMethod(): MethodSpec = fillByIntentBinding("service")

    override fun createStarters(variant: List<ArgumentBinding>): List<MethodSpec> = listOf(
            createGetIntentMethod(variant),
            createStartServiceMethod(variant)
    )

    private fun createStartServiceMethod(variant: List<ArgumentBinding>) =
            createGetIntentStarter("startService", variant)
}