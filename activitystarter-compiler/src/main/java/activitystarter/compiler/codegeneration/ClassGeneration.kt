package activitystarter.compiler.codegeneration

import activitystarter.compiler.model.classbinding.ClassBinding
import activitystarter.compiler.model.param.ArgumentBinding
import activitystarter.compiler.utils.CONTEXT
import activitystarter.compiler.utils.STRING
import activitystarter.wrapping.ArgWrapper
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Modifier
import javax.lang.model.element.Modifier.*

internal abstract class ClassGeneration(val classBinding: ClassBinding) {

    fun brewJava() = JavaFile.builder(classBinding.packageName, createStarterSpec())
            .addFileComment("Generated code from ActivityStarter. Do not modify!")
            .build()

    abstract fun createFillFieldsMethod(): MethodSpec

    open fun TypeSpec.Builder.addExtraToClass(): TypeSpec.Builder = this

    abstract fun createStarters(variant: List<ArgumentBinding>): List<MethodSpec>

    protected fun getBasicFillMethodBuilder(fillProperCall: String = "ActivityStarter.fill(this)"): MethodSpec.Builder = MethodSpec
            .methodBuilder("fill")
            .addJavadoc("This is method used to fill fields. Use it by calling $fillProperCall.")
            .addModifiers(PUBLIC, Modifier.STATIC)

    protected fun builderWithCreationBasicFields(name: String) =
            builderWithCreationBasicFieldsNoContext(name)
                    .addParameter(CONTEXT, "context")

    protected fun builderWithCreationBasicFieldsNoContext(name: String) =
            MethodSpec.methodBuilder(name)
                    .addModifiers(PUBLIC, Modifier.STATIC)

    protected fun MethodSpec.Builder.addArgParameters(variant: List<ArgumentBinding>) = apply {
        variant.forEach { arg -> addParameter(arg.typeName, arg.name) }
    }

    protected fun MethodSpec.Builder.addSaveBundleStatements(bundleName: String, variant: List<ArgumentBinding>, argumentGetByName: (ArgumentBinding) -> String) = apply {
        variant.forEach { arg ->
            val bundleSetter = getBundleSetterFor(arg.paramType)
            addStatement("$bundleName.$bundleSetter(" + arg.fieldName + ", " + argumentGetByName(arg) + ")")
        }
    }

    protected fun MethodSpec.Builder.addBundleSetters(bundleName: String, className: String, checkIfSet: Boolean) = apply {
        classBinding.argumentBindings.forEach { arg -> addBundleSetter(arg, bundleName, className, checkIfSet) }
    }

    protected fun MethodSpec.Builder.addBundleSetter(arg: ArgumentBinding, bundleName: String, className: String, checkIfSet: Boolean) {
        val fieldName = arg.fieldName
        var bundleValue = getBundleGetter(bundleName, arg.paramType, arg.typeName, fieldName)
        if (arg.converter != null) bundleValue = addUnwrapper(bundleValue, arg)
        val bundleValueSetter = arg.accessor.makeSetter(bundleValue)
        if (checkIfSet) addCode("if(${getBundlePredicate(bundleName, fieldName)}) ")
        addStatement("$className.$bundleValueSetter")
    }

    private fun MethodSpec.Builder.addUnwrapper(bundleValue: String, arg: ArgumentBinding): String {
        val nameAfterUnwrap = "unwrapped"
//        val unwrappedType = converter.addStatement("auto $nameAfterUnwrap = new \$T().unwrap($bundleValue)", converter)
        return nameAfterUnwrap
    }

    private fun MethodSpec.Builder.addWrapper(bundleValue: String, converter: Class<out ArgWrapper<*, *>>): String {
        val nameAfterWrap = "wrapped"
        addStatement("$nameAfterWrap = new \$T()", converter)
        return nameAfterWrap
    }

    protected fun getBundlePredicate(bundleName: String, key: String) = "$bundleName.containsKey($key)"

    private fun createStarterSpec() = TypeSpec
            .classBuilder(classBinding.bindingClassName.simpleName())
            .addModifiers(PUBLIC, FINAL)
            .addKeyFields()
            .addClassMethods()
            .build()

    private fun TypeSpec.Builder.addKeyFields(): TypeSpec.Builder {
        for (arg in classBinding.argumentBindings) {
            val fieldSpec = FieldSpec
                    .builder(STRING, arg.fieldName, STATIC, FINAL, PRIVATE)
                    .initializer("\"${arg.key}\"")
                    .build()
            addField(fieldSpec)
        }
        return this
    }

    private fun TypeSpec.Builder.addClassMethods() = this
            .addMethod(createFillFieldsMethod())
            .addExtraToClass()
            .addMethods(classBinding.argumentBindingVariants.flatMap { variant -> createStarters(variant) })
}
