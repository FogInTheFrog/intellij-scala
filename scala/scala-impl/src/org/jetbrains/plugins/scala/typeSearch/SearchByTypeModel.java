package org.jetbrains.plugins.scala.typeSearch;

import com.intellij.ide.util.gotoByName.FilteringGotoByModel;
import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SearchByTypeModel extends FilteringGotoByModel<String> {
    protected SearchByTypeModel(@NotNull Project project, List<ChooseByNameContributor> contributors) {
        super(project, contributors);
    }

    @Nullable
    @Override
    protected String filterValueFor(NavigationItem item) {
        return null;
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Sentence) String getPromptText() {
        return "Enter the type";
    }

    @Override
    public @NotNull
    @NlsContexts.Label String getNotInMessage() { return "No matches found"; }

    @Override
    public @NotNull
    @NlsContexts.Label String getNotFoundMessage() { return "Type not found"; }

    @Override
    public @Nullable
    @NlsContexts.Label String getCheckBoxName() { return null; }

    @Override
    public boolean loadInitialCheckBoxState() { return false; }

    @Override
    public void saveInitialCheckBoxState(boolean state) { }

    @NotNull
    @Override
    public String[] getSeparators() {
        return new String[0];
    }

    @Override
    public @Nullable String getFullName(@NotNull Object element) {
        return null;
    }

    @Override
    public boolean willOpenEditor() {
        return false;
    }
}
