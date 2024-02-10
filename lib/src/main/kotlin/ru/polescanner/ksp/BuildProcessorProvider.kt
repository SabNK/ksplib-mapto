package ru.polescanner.ksp

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class BuildProcessorProvider: SymbolProcessorProvider{
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        BuildProcessor(environment.codeGenerator, environment.logger)
}