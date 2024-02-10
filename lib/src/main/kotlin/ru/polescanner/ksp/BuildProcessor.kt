package ru.polescanner.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import kotlin.reflect.KClass


class BuildProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbols(MapTo::class)
        val s = symbols.toList().size
        logger.info("resolved $s @MapTo cases")


        val fileSpecMap = emptyMap<Pair<String, String>, FileSpec.Builder>().toMutableMap()

        val t = symbols.filter { it.validate() }
        val ss = t.toList().size
        logger.info("validated $ss resolved @MapTo cases")
        t.forEach { it.accept(VisitorMapper(fileSpecMap), Unit) }
        fileSpecMap.forEach{it.value.build().writeTo(codeGenerator = codeGenerator, aggregating = false)}
        return symbols.filterNot { it.validate() }.toList()
    }
    private inner class VisitorMapper(val fileSpecMap: MutableMap<Pair<String, String>, FileSpec.Builder>): KSVisitorVoid() {
        private lateinit var ksType: KSType
        private lateinit var packageName: String

        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            classDeclaration.qualifiedName?.asString() ?: run {
                logger.error(
                    "@MapTo must target classes with qualified names",
                    classDeclaration
                )
                return
            }

            ksType = classDeclaration.asType(emptyList())
            packageName = classDeclaration.packageName.asString()

            val iMapper = classDeclaration.superTypes.first().resolve()

            if (iMapper.arguments.size != 2) {
                logger.error(
                    "IMapper must be of two arguments type",
                    classDeclaration
                )
                return
            }
            val domain = iMapper.arguments[0].toTypeName(TypeParameterResolver.EMPTY)

            val projection = iMapper.arguments[1].toTypeName(TypeParameterResolver.EMPTY)

            val fileName = projection.toString().removePackageName(packageName).substringBefore('.') + "Ext"
            logger.info("prepare generating package: $packageName file: $fileName", classDeclaration)

            val builder = fileSpecMap[packageName to fileName]?: FileSpec.builder(packageName, fileName)
            fileSpecMap[packageName to fileName] = genFile(builder, domain, projection)
        }
        private fun KSClassDeclaration.isDataClass() =
            modifiers.contains(Modifier.DATA)


    }
    private fun genFile(builder: FileSpec.Builder,
                        domain: TypeName,
                        projection: TypeName): FileSpec.Builder {
        val mapFrom = "Domain"
        val simpleDomain = domain.toString().reversed().substringBefore('.').reversed()
        logger.info("domain simple: $simpleDomain type: $domain")
        val simpleProjection = projection.toString().reversed().substringBefore('.').reversed()
        logger.info("projection simple: $simpleProjection type: $projection")
        val mapTo: String = simpleProjection.substringAfter(simpleDomain)

        return builder
            .addFunction(
                FunSpec.builder("to$mapTo")
                    .receiver(domain)
                    .returns(projection)
                    .addStatement("return ${projection}.Mapper.mapTo(this)")
                    .build()
            )
            .addFunction(
                FunSpec.builder("to$mapFrom")
                    .receiver(projection)
                    .returns(domain)
                    .addStatement("return ${projection}.Mapper.mapFrom(this)")
                    .build()
            )
    }

}


fun Resolver.getSymbols(cls: KClass<*>) =
    this.getSymbolsWithAnnotation(cls.qualifiedName!!)
        .filterIsInstance<KSClassDeclaration>()


private fun String.removePackageName(packageName: String) = substringAfter(packageName + '.')
