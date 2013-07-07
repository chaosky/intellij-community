/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.plugins.groovy.console;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiVariable;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.NameHint;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.debugger.fragments.GroovyCodeFragment;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;
import org.jetbrains.plugins.groovy.lang.psi.impl.synthetic.GrLightVariable;

import java.util.Map;

/**
 * Created by Max Medvedev on 9/8/13
 */
public class GroovyShellCodeFragment extends GroovyCodeFragment {
  private final Map<String, PsiVariable> myVariables = ContainerUtil.newHashMap();

  public GroovyShellCodeFragment(Project project, CharSequence text) {
    super(project, text);
  }

  public GroovyShellCodeFragment(Project project, LightVirtualFile virtualFile) {
    super(project, virtualFile);
  }


  public void addVariable(String name, GrExpression expr) {
    myVariables.put(name, new GrLightVariable(getManager(), name, expr.getType(), this));
  }

  public void addVariable(String name, PsiType type) {
    myVariables.put(name, new GrLightVariable(getManager(), name, type, this));
  }

  @Override
  public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                     @NotNull ResolveState state,
                                     PsiElement lastParent,
                                     @NotNull PsiElement place) {
    if (!super.processDeclarations(processor, state, lastParent, place)) {
      return false;
    }

    if (!processVariables(processor, state)) {
      return false;
    }

    return true;
  }

  private boolean processVariables(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state) {
    NameHint nameHint = processor.getHint(NameHint.KEY);
    String name = nameHint != null ? nameHint.getName(state) : null;

    if (name != null) {
      final PsiVariable var = myVariables.get(name);
      if (var != null) {
        if (processor.execute(var, state)) {
          return false;
        }
      }
    }
    else {
      for (PsiVariable var : myVariables.values()) {
        if (!processor.execute(var, state)) {
          return false;
        }
      }
    }

    return true;
  }
}
