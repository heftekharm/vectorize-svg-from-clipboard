/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.heftekharm.vectorizesvgfromclipboard;

import com.android.tools.idea.npw.assetstudio.IconGenerator;
import com.android.tools.idea.npw.assetstudio.wizard.GenerateIconsModel;
import com.android.tools.idea.npw.assetstudio.wizard.PersistentState;
import com.android.tools.idea.projectsystem.NamedModuleTemplate;
import com.android.tools.idea.wizard.model.WizardModel;
import com.google.common.collect.ImmutableList;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;


public final class CustomGenerateIconsModel extends WizardModel {
  @Nullable private IconGenerator myIconGenerator;
  @NotNull private NamedModuleTemplate myTemplate;
  @NotNull private File myResFolder;
  @NotNull private List<File> myFilesToDelete = ImmutableList.of();
  @NotNull private final GenerateIconsModel.StateStorage myStateStorage;
  @NotNull private final String myWizardId;

  /**
   * Initializes the model.
   *
   * @param wizardId the id of the wizard owning the model. Used as a key for storing wizard state.
   * @param template of the default flavor
   * @param resFolder the default output folder
   */
  public CustomGenerateIconsModel(
    @NotNull Project project,
    @NotNull String wizardId,
    @NotNull NamedModuleTemplate template,
    @NotNull File resFolder
  ) {
    myTemplate = template;
    myResFolder = resFolder;
    myStateStorage = GenerateIconsModel.StateStorage.getInstance(project);
    assert myStateStorage != null;
    myWizardId = wizardId;
  }

  public void setIconGenerator(@NotNull IconGenerator iconGenerator) {
    myIconGenerator = iconGenerator;
  }

  @Nullable
  public IconGenerator getIconGenerator() {
    return myIconGenerator;
  }

  public void setTemplate(@NotNull NamedModuleTemplate template) {
    myTemplate = template;
  }

  @NotNull
  public NamedModuleTemplate getTemplate() {
    return myTemplate;
  }

  public void setResFolder(@NotNull File resFolder) {
    myResFolder = resFolder;
  }

  public File getResFolder() {
    return myResFolder;
  }

  public void setFilesToDelete(@NotNull List<File> files) {
    myFilesToDelete = ImmutableList.copyOf(files);
  }

  @Override
  protected void handleFinished() {
    if (myIconGenerator == null) {
      getLog().error("GenerateIconsModel did not collect expected information and will not complete. Please report this error.");
      return;
    }

    myIconGenerator.generateIconsToDisk(myTemplate.getPaths(), myResFolder);
    for (File file : myFilesToDelete) {
      //noinspection ResultOfMethodCallIgnored
      file.delete();
    }
  }

  /**
   * Returns the persistent state associated with the wizard.
   */
  @NotNull
  public PersistentState getPersistentState() {
    return myStateStorage.getState().getOrCreateChild(myWizardId);
  }

  @NotNull
  private static Logger getLog() {
    return Logger.getInstance(CustomGenerateIconsModel.class);
  }

}
