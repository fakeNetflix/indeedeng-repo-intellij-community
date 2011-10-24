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
package org.jetbrains.plugins.groovy.formatter;

import com.intellij.formatting.Alignment;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrClosableBlock;
import org.jetbrains.plugins.groovy.lang.psi.util.PsiUtil;

import java.util.Map;

import static org.jetbrains.plugins.groovy.lang.lexer.GroovyTokenTypes.mRCURLY;
import static org.jetbrains.plugins.groovy.lang.lexer.TokenSets.WHITE_SPACES_SET;
import static org.jetbrains.plugins.groovy.lang.parser.GroovyElementTypes.CLOSABLE_BLOCK;

/**
 * @author Max Medvedev
 */
public class GeeseUtil {
  private static final Logger LOG = Logger.getInstance(GeeseUtil.class);

  private GeeseUtil() {
  }

  @Nullable
  public static ASTNode getClosureRBraceAtTheEnd(ASTNode node) {
    IElementType elementType = node.getElementType();
    if (elementType == CLOSABLE_BLOCK) {
      PsiElement rBrace = ((GrClosableBlock)node.getPsi()).getRBrace();
      return rBrace != null ? rBrace.getNode() : null;
    }

    ASTNode lastChild = node.getLastChildNode();
    while (lastChild != null && WHITE_SPACES_SET.contains(lastChild.getElementType())) {
      lastChild = lastChild.getTreePrev();
    }
    if (lastChild == null) return null;

    return getClosureRBraceAtTheEnd(lastChild);
  }

  public static boolean isClosureRBrace(PsiElement e) {
    return e != null && e.getNode().getElementType() == mRCURLY &&
           e.getParent() instanceof GrClosableBlock &&
           ((GrClosableBlock)e.getParent()).getRBrace() == e;
  }

  @Nullable
  public static PsiElement getNextNonWhitespaceToken(PsiElement e) {
    PsiElement next = PsiTreeUtil.nextLeaf(e);
    while (next != null && next.getNode().getElementType() == TokenType.WHITE_SPACE) next = PsiTreeUtil.nextLeaf(next);
    return next;
  }

  @Nullable
  public static PsiElement getPreviousNonWhitespaceToken(PsiElement e) {
    PsiElement next = PsiTreeUtil.prevLeaf(e);
    while (next != null && next.getNode().getElementType() == TokenType.WHITE_SPACE) next = PsiTreeUtil.prevLeaf(next);
    return next;
  }

  static Alignment calculateRBraceAlignment(PsiElement psi, Map<PsiElement, Alignment> alignments) {
    int leadingBraceCount = 0;
    PsiElement next = psi;
    while (isClosureRBrace(next = getPreviousNonWhitespaceToken(next))) {
      leadingBraceCount++;
    }

    PsiElement cur = psi;
    while (isClosureRBrace(next = getNextNonWhitespaceToken(cur))) {
      cur = next;
    }

    for (; leadingBraceCount > 0; leadingBraceCount--) {
      cur = getPreviousNonWhitespaceToken(cur);
    }

    PsiElement parent = cur.getParent();
    LOG.assertTrue(parent instanceof GrClosableBlock);

    //search for start of the line
    cur = parent;
    while (!PsiUtil.isNewLine(next = PsiTreeUtil.prevLeaf(cur, true))) {
      if (next == null) break;
      cur = next;
    }

    //PsiElement statement = PsiUtil.findEnclosingStatement(parent);
    Alignment alignment = alignments.get(cur);
    if (alignment == null) {
      alignment = Alignment.createAlignment(true);
      alignments.put(cur, alignment);
    }
    return alignment;
  }
}