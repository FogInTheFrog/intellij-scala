package org.jetbrains.plugins.scala.typeSearch;

import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.openapi.extensions.ExtensionPointName;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class Extensions {
    private static final String EXTENSION_POINT_NAME = "org.jetbrains.plugins.scala.searchByTypeContributor";
    private @NotNull
    static final ExtensionPointName<ChooseByNameContributor> extensionPoints = ExtensionPointName.create(EXTENSION_POINT_NAME);

    public static List<ChooseByNameContributor> getExtensions()  {
        return extensionPoints.getExtensionList();
    }
}