// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.python.sdk.pipenv

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.SdkAdditionalData
import com.intellij.openapi.util.UserDataHolder
import com.jetbrains.python.PyBundle
import com.jetbrains.python.PyPsiBundle
import com.jetbrains.python.packaging.pipenv.PyPipEnvPackageManagementService
import com.jetbrains.python.packaging.ui.PyPackageManagementService
import com.jetbrains.python.psi.PyFile
import com.jetbrains.python.sdk.PyInterpreterInspectionQuickFixData
import com.jetbrains.python.sdk.PySdkProvider
import com.jetbrains.python.sdk.PythonSdkUtil
import com.jetbrains.python.sdk.add.PyAddNewEnvPanel
import com.jetbrains.python.sdk.pipenv.UsePipEnvQuickFix.Companion.isApplicable
import org.jdom.Element
import javax.swing.Icon

class PyPipEnvSdkProvider : PySdkProvider {
  override val configureSdkProgressText: String
    get() = PyBundle.message("looking.for.pipfile")

  override fun configureSdk(project: Project, module: Module, existingSdks: List<Sdk>): Sdk? {
    return detectAndSetupPipEnv(project, module, existingSdks)
  }

  override fun getSdkAdditionalText(sdk: Sdk): String? = if (sdk.isPipEnv) sdk.versionString else null

  override fun getSdkIcon(sdk: Sdk): Icon? = if (sdk.isPipEnv) PIPENV_ICON else null

  override fun loadAdditionalDataForSdk(element: Element): SdkAdditionalData? {
    return PyPipEnvSdkAdditionalData.load(element)
  }

  override fun tryCreatePackageManagementServiceForSdk(project: Project, sdk: Sdk): PyPackageManagementService? {
    return if (sdk.isPipEnv) PyPipEnvPackageManagementService(project, sdk) else null
  }

  override fun createMissingSdkFix(module: Module, file: PyFile): PyInterpreterInspectionQuickFixData? = when {
    isApplicable(module) -> PyInterpreterInspectionQuickFixData(
      UsePipEnvQuickFix(null, module), PyPsiBundle.message("python.sdk.no.interpreter.configured.owner", "project"))
    else -> null
  }

  override fun createEnvironmentAssociationFix(module: Module, sdk: Sdk, isPyCharm: Boolean, associatedModulePath: String?): PyInterpreterInspectionQuickFixData? {
    if (sdk.isPipEnv) {
      val message = when {
        associatedModulePath != null -> when {
          isPyCharm -> "Pipenv interpreter is associated with another project: '$associatedModulePath'"
          else -> "Pipenv interpreter is associated with another module: '$associatedModulePath'"
        }
        else -> when {
          isPyCharm -> "Pipenv interpreter is not associated with any project"
          else -> "Pipenv interpreter is not associated with any module"
        }
      }
      return PyInterpreterInspectionQuickFixData(UsePipEnvQuickFix(sdk, module), message)
    }
    return null
  }

  override fun createInstallPackagesQuickFix(module: Module): LocalQuickFix? {
    val sdk = PythonSdkUtil.findPythonSdk(module) ?: return null
    return if (sdk.isPipEnv) PipEnvInstallQuickFix() else null
  }

  override fun createNewEnvironmentPanel(project: Project?,
                                         module: Module?,
                                         existingSdks: List<Sdk>,
                                         newProjectPath: String?,
                                         context: UserDataHolder): PyAddNewEnvPanel {
    return PyAddPipEnvPanel(null, null, existingSdks, newProjectPath, context)
  }
}