package com.jetbrains.python.run;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.execution.configurations.ParamsGroup;
import com.intellij.execution.filters.Filter;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.util.text.StringUtil;

import java.util.List;

/**
 * @author yole
 */
public class PythonScriptCommandLineState extends PythonCommandLineState {
  private final PythonRunConfiguration myConfig;

  public PythonScriptCommandLineState(PythonRunConfiguration runConfiguration, ExecutionEnvironment env, List<Filter> filters) {
    super(runConfiguration, env, filters);
    myConfig = runConfiguration;
  }

  @Override
  protected void buildCommandLineParameters(GeneralCommandLine commandLine) {
    ParametersList parametersList = commandLine.getParametersList();
    ParamsGroup exe_options = parametersList.getParamsGroup(GROUP_EXE_OPTIONS);
    assert exe_options != null;
    exe_options.addParametersString(myConfig.getInterpreterOptions());

    ParamsGroup script_parameters = parametersList.getParamsGroup(GROUP_SCRIPT);
    assert script_parameters != null;
    if (!StringUtil.isEmptyOrSpaces(myConfig.getScriptName())) {
      script_parameters.addParameter(myConfig.getScriptName());
    }

    final String script_options_string = myConfig.getScriptParameters();
    if (script_options_string != null) script_parameters.addParametersString(script_options_string);

    if (!StringUtil.isEmptyOrSpaces(myConfig.getWorkingDirectory())) {
      commandLine.setWorkDirectory(myConfig.getWorkingDirectory());
    }
  }

}
