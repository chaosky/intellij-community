// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.dvcs.push.ui;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Any OK-action in the push dialog must inherit from this base class.
 */
public abstract class PushActionBase extends DumbAwareAction {

  public PushActionBase(@NotNull String actionName) {
    super(actionName);
  }

  /**
   * A marker interface indicating an action which should be treated as default in the push dialog, instead of {@link VcsPushDialog.SimplePushAction}.
   * Can be implemented by plugins to override the default behavior.
   */
  @ApiStatus.Internal
  public interface DefaultPushAction {
    default void customize(@NotNull List<PushActionBase> pushActions) {
      pushActions.add(0, (PushActionBase) this);
    }
  }

  protected PushActionBase() {
    setEnabledInModalContext(true);
  }

  protected abstract boolean isEnabled(@NotNull VcsPushUi dialog);

  @Nls
  @Nullable
  protected abstract String getDescription(@NotNull VcsPushUi dialog, boolean enabled);

  protected abstract void actionPerformed(@NotNull Project project, @NotNull VcsPushUi dialog);

  @Override
  public final void actionPerformed(@NotNull AnActionEvent e) {
    actionPerformed(e.getRequiredData(CommonDataKeys.PROJECT), e.getRequiredData(VcsPushUi.VCS_PUSH_DIALOG));
  }

  @Override
  public final void update(@NotNull AnActionEvent e) {
    VcsPushUi dialog = e.getData(VcsPushUi.VCS_PUSH_DIALOG);
    if (dialog == null || e.getProject() == null) {
      e.getPresentation().setEnabledAndVisible(false);
      return;
    }
    e.getPresentation().setVisible(true);

    boolean enabled = isEnabled(dialog);
    e.getPresentation().setEnabled(enabled);
    e.getPresentation().setDescription(getDescription(dialog, enabled));
  }
}
