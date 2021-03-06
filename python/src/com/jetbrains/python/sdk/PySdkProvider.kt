// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.python.sdk

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.SdkAdditionalData
import com.intellij.openapi.util.UserDataHolder
import com.jetbrains.python.packaging.ui.PyPackageManagementService
import com.jetbrains.python.psi.PyFile
import com.jetbrains.python.sdk.add.PyAddNewEnvPanel
import org.jdom.Element
import org.jetbrains.annotations.ApiStatus
import javax.swing.Icon

/**
 * This API is subject to change in version 2020.3, please avoid using it. If you have to, your plugin has to set compatibility to 2020.2.2.
 */
@ApiStatus.Experimental
interface PySdkProvider {
  // SDK
  val configureSdkProgressText: String

  /**
   * Try to create SDK for the module and return it or null.
   */
  fun configureSdk(project: Project, module: Module, existingSdks: List<Sdk>): Sdk?

  /**
   * Additional info to be displayed with the SDK's name.
   */
  fun getSdkAdditionalText(sdk: Sdk): String?

  fun getSdkIcon(sdk: Sdk): Icon?

  /**
   * Try to load additional data for your SDK. Check for attributes, specific to your SDK before loading it. Return null if there is none.
   */
  fun loadAdditionalDataForSdk(element: Element): SdkAdditionalData?

  // Packaging
  fun tryCreatePackageManagementServiceForSdk(project: Project, sdk: Sdk): PyPackageManagementService?

  // Inspections
  /**
   * Creates quickfix to create new environment. Please make sure that it's your provider that has to create new environment for the module,
   * i.e. check for the presence of certain files (like Pipfile or pyproject.toml). Return null otherwise.
   */
  fun createMissingSdkFix(module: Module, file: PyFile): PyInterpreterInspectionQuickFixData?

  /**
   * Quickfix that makes the existing environment available to the module, or null.
   */
  fun createEnvironmentAssociationFix(module: Module,
                                      sdk: Sdk,
                                      isPyCharm: Boolean,
                                      associatedModulePath: String?): PyInterpreterInspectionQuickFixData?

  fun createInstallPackagesQuickFix(module: Module): LocalQuickFix?


  // New env
  fun createNewEnvironmentPanel(project: Project?,
                                module: Module?,
                                existingSdks: List<Sdk>,
                                newProjectPath: String?,
                                context: UserDataHolder): PyAddNewEnvPanel


  companion object {
    @JvmField
    val EP_NAME  = ExtensionPointName.create<PySdkProvider>("Pythonid.pySdkProvider")
  }
}

@ApiStatus.Experimental
data class PyInterpreterInspectionQuickFixData(val quickFix: LocalQuickFix, val message: String)