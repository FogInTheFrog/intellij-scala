package org.jetbrains.sbt.project.structure

import com.intellij.execution.configurations.{GeneralCommandLine, SimpleJavaParameters}
import com.intellij.execution.target.local.LocalTargetEnvironmentRequest
import com.intellij.execution.target.{TargetEnvironmentRequest, TargetProgressIndicator, TargetedCommandLineBuilder}
import com.intellij.execution.wsl.WslPath
import com.intellij.execution.wsl.target.WslTargetEnvironmentRequest
import com.intellij.execution.{CantRunException, ExecutionBundle}
import com.intellij.openapi.projectRoots.JdkCommandLineSetup

import java.io.File

/**
 * The same as [[SimpleJavaParameters]] but based on path to java executable instead of JDK
 * @note this is due to we use JrePathSelector instead of SdkComboBox in sbt settings, we might consider rewriting it
 */
final class MySimpleJavaParameters extends SimpleJavaParameters {

  var vmExecutable: Option[File] = None

  @throws[CantRunException]
  override def toCommandLine: GeneralCommandLine = {
    val request = new LocalTargetEnvironmentRequest
    val builder = MyJdkUtils.setupJVMCommandLine(this, request)
    val environment = request.prepareEnvironment(TargetProgressIndicator.EMPTY)
    environment.createGeneralCommandLine(builder.build)
  }

  /**
   * @throws CantRunException when incorrect Java SDK is specified
   * @see JdkUtil#setupJVMCommandLine(SimpleJavaParameters)
   */
  @throws[CantRunException]
  override def toCommandLine(request: TargetEnvironmentRequest): TargetedCommandLineBuilder = {
    MyJdkUtils.setupJVMCommandLine(this, request)
  }
}


//noinspection ApiStatus,UnstableApiUsage
private object MyJdkUtils {


  /** analog of [[com.intellij.openapi.projectRoots.JdkUtil.setupJVMCommandLine]] */
  @throws[CantRunException]
  def setupJVMCommandLine(javaParameters: MySimpleJavaParameters): GeneralCommandLine = {
    val request = new LocalTargetEnvironmentRequest
    val builder = setupJVMCommandLine(javaParameters, request)
    val environment = request.prepareEnvironment(TargetProgressIndicator.EMPTY)
    environment.createGeneralCommandLine(builder.build)
  }

  /** analog of [[com.intellij.openapi.projectRoots.JdkUtil.setupJVMCommandLine]] */
  @throws[CantRunException]
  def setupJVMCommandLine(javaParameters: MySimpleJavaParameters, request: TargetEnvironmentRequest): TargetedCommandLineBuilder = {
    val setup = new JdkCommandLineSetup(request)
    val javaExePath = getJavaExePathOnTarget(javaParameters, request)
    setup.getCommandLine.setExePath(javaExePath)
    setup.setupCommandLine(javaParameters)
    setup.getCommandLine
  }

  /** Analog of [[com.intellij.openapi.projectRoots.JdkCommandLineSetup.setupJavaExePath]] */
  @throws[CantRunException]
  private def getJavaExePathOnTarget(
    javaParameters: MySimpleJavaParameters,
    request: TargetEnvironmentRequest
  ): String = {
    val javaExecutable = javaParameters.vmExecutable.getOrElse {
      throw new CantRunException(ExecutionBundle.message("run.configuration.error.no.jdk.specified"))
    }
    val path = javaExecutable.getPath
    val pathInTarget = request match {
      case _: WslTargetEnvironmentRequest =>
        val pathInWsl = WslPath.parseWindowsUncPath(path)
        if (pathInWsl != null)
          pathInWsl.getLinuxPath
        else
          path
      case _ =>
        path
    }
    pathInTarget
  }
}
