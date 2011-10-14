/*
 * Copyright 2000-2011 JetBrains s.r.o.
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
package com.intellij.core;

import com.intellij.ide.highlighter.JavaClassFileType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.JavaPsiFacadeImpl;

import java.io.File;

/**
 * @author yole
 */
public class JavaCoreEnvironment extends CoreEnvironment {
  private final CoreJavaFileManager myFileManager;
  
  public JavaCoreEnvironment(Disposable parentDisposable) {
    super(parentDisposable);

    registerFileType(JavaClassFileType.INSTANCE, "class");
    addExplicitExtension(FileTypeFileViewProviders.INSTANCE, JavaClassFileType.INSTANCE,  new ClassFileViewProviderFactory());

    registerProjectExtensionPoint(PsiElementFinder.EP_NAME, PsiElementFinder.class);
    myFileManager = new CoreJavaFileManager(myPsiManager, myJarFileSystem);
    JavaPsiFacadeImpl javaPsiFacade = new JavaPsiFacadeImpl(myProject, myPsiManager, myFileManager, null);
    registerComponentInstance(myProject.getPicoContainer(),
                              JavaPsiFacade.class,
                              javaPsiFacade);
    myProject.registerService(JavaPsiFacade.class, javaPsiFacade);
  }

  public void addToClasspath(File path) {
    myFileManager.addToClasspath(path);
    final VirtualFile root = myJarFileSystem.findFileByPath(path + "!/");
    if (root != null) {
      myFileIndexFacade.addLibraryRoot(root);
    }
  }
}