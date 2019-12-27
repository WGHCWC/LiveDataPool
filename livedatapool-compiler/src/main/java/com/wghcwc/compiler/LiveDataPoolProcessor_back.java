package com.wghcwc.compiler;


import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.wghcwc.livedatapool_annotation.BackgroundObj;
import com.wghcwc.livedatapool_annotation.Push;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * @author hcDarren
 * @date 2017/9/9
 */

public class LiveDataPoolProcessor_back extends AbstractProcessor {
    private Filer mFiler;
    private Elements mElementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnvironment.getFiler();
        mElementUtils = processingEnvironment.getElementUtils();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        for (Class<? extends Annotation> annotation : getSupportedAnnotations()) {
            types.add(annotation.getCanonicalName());
        }
        return types;
    }

    private Set<Class<? extends Annotation>> getSupportedAnnotations() {
        Set<Class<? extends Annotation>> annotations = new LinkedHashSet<>();
        annotations.add(BackgroundObj.class);
        annotations.add(Push.class);
        return annotations;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {


        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Push.class);

        Map<Element, List<Element>> elementsMap = new LinkedHashMap<>();
        for (Element element : elements) {
            Element enclosingElement = element.getEnclosingElement();

            List<Element> viewBindElements = elementsMap.get(enclosingElement);
            if (viewBindElements == null) {
                viewBindElements = new ArrayList<>();
                elementsMap.put(enclosingElement, viewBindElements);
            }
            viewBindElements.add(element);
        }

        // 生成代码
        for (Map.Entry<Element, List<Element>> entry : elementsMap.entrySet()) {
            Element enclosingElement = entry.getKey();
            List<Element> viewBindElements = entry.getValue();

            // public final class xxxActivity_ViewBinding implements Unbinder
            String activityClassNameStr = enclosingElement.getSimpleName().toString();
            ClassName activityClassName = ClassName.bestGuess(activityClassNameStr);
            ClassName unbinderClassName = ClassName.get("com.butterknife", "Unbinder");
            TypeSpec.Builder classBuilder = TypeSpec.classBuilder(activityClassNameStr + "_ViewBinding")
                    .addModifiers(Modifier.FINAL, Modifier.PUBLIC).addSuperinterface(unbinderClassName)
                    .addField(activityClassName, "target", Modifier.PRIVATE);


            // 实现 unbind 方法
            // android.support.annotation.CallSuper
            ClassName callSuperClassName = ClassName.get("android.support.annotation", "CallSuper");
            MethodSpec.Builder unbindMethodBuilder = MethodSpec.methodBuilder("unbind")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addAnnotation(callSuperClassName);

            unbindMethodBuilder.addStatement("$T target = this.target", activityClassName);
            unbindMethodBuilder.addStatement("if (target == null) throw new IllegalStateException(\"Bindings already cleared.\");");

            // 构造函数
            MethodSpec.Builder constructorMethodBuilder = MethodSpec.constructorBuilder()
                    .addParameter(activityClassName, "target");
            // this.target = target;
            constructorMethodBuilder.addStatement("this.target = target");
            // findViewById 属性
            for (Element viewBindElement : viewBindElements) {
                // target.textView1 = Utils.findRequiredViewAsType(source, R.id.tv1, "field 'textView1'", TextView.class);
                // target.textView1 = Utils.findViewById(source, R.id.tv1);
                String filedName = viewBindElement.getSimpleName().toString();
                ClassName utilsClassName = ClassName.get("com.butterknife", "Utils");
//                int resId = viewBindElement.getAnnotation(Push.class).value();
                constructorMethodBuilder.addStatement("target.$L = $T.findViewById(target, $L)", filedName, utilsClassName, 0);
                // target.textView1 = null;
                unbindMethodBuilder.addStatement("target.$L = null", filedName);
            }


            classBuilder.addMethod(unbindMethodBuilder.build());
            classBuilder.addMethod(constructorMethodBuilder.build());

            // 生成类，看下效果
            try {
                String packageName = mElementUtils.getPackageOf(enclosingElement).getQualifiedName().toString();

                JavaFile.builder(packageName, classBuilder.build())
                        .addFileComment("butterknife 自动生成")
                        .build().writeTo(mFiler);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("翻车了！");
            }
        }
        return false;
    }
}

