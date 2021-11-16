package org.jetbrains.plugins.scala.typeSearch;
import com.intellij.ide.actions.searcheverywhere.AbstractGotoSEContributor;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributor;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributorFactory;
import com.intellij.ide.util.gotoByName.FilteringGotoByModel;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class SearchByTypeSearchEverywhereContributor extends AbstractGotoSEContributor {

        protected SearchByTypeSearchEverywhereContributor(@NotNull AnActionEvent event) {
                super(event);
        }

        @Override
        protected @NotNull FilteringGotoByModel<String> createModel(@NotNull Project project) {
                return new SearchByTypeModel(project, Extensions.getExtensions());
        }


        @Override
        public @NotNull
        @Nls String getGroupName() {
                return "Functions by type";
        }

        @Override
        public int getSortWeight() {
                return 1000;
        }

        static class Factory implements SearchEverywhereContributorFactory {
                @Override
                public @NotNull SearchEverywhereContributor createContributor(@NotNull AnActionEvent initEvent) {
                        return new SearchByTypeSearchEverywhereContributor(initEvent);
                }
        }

}



