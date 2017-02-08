package activitystarter.compiler.classbinding

import activitystarter.compiler.ArgumentBinding
import activitystarter.compiler.CONTEXT
import activitystarter.compiler.INTENT
import activitystarter.compiler.isSubtypeOfType
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.Modifier.STATIC
import javax.lang.model.element.TypeElement

internal class ServiceBinding(element: TypeElement) : IntentBinding(element) {

    override fun createFillFieldsMethod(): MethodSpec {
        val builder = getBasicFillMethodBuilder("ActivityStarter.fill(this, intent)")
                .addParameter(targetTypeName, "service")
                .addParameter(INTENT, "intent")

        builder.addSetters("service")
        return builder.build()
    }

    override fun createStarters(variant: List<ArgumentBinding>): List<MethodSpec> = listOf(
            createGetIntentMethod(variant),
            createStartServiceMethod(variant)
    )

    private fun createStartServiceMethod(variant: List<ArgumentBinding>)
            = createGetIntentStarter("startService", variant)
}