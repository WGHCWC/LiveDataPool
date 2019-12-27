package com.wghcwc.compiler;


import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.wghcwc.livedatapool_annotation.BackgroundObj;
import com.wghcwc.livedatapool_annotation.Push;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
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

public class LiveDataPoolProcessor extends AbstractProcessor {
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


        Set<? extends Element> multiples = roundEnvironment.getElementsAnnotatedWith(BackgroundObj.class);
        Set<? extends Element> pushs = roundEnvironment.getElementsAnnotatedWith(Push.class);
        Map<Element, List<Element>> pushListMap = new HashMap<>();
        for (Element element : pushs) {
            Element className = element.getEnclosingElement();
            List<Element> childField = pushListMap.get(className);
            if (childField == null) {
                childField = new ArrayList<>();
                pushListMap.put(className, childField);
            }
            childField.add(element);
        }

        ClassName liveDataPool = ClassName.get("com.wghcwc.livedata_pool", "LiveDataPool");
        for (Map.Entry<Element, List<Element>> elementListEntry : pushListMap.entrySet()) {
            Element enCLoseElement = elementListEntry.getKey();
            List<Element> fieldElement = elementListEntry.getValue();
            String classNameStr = enCLoseElement.getSimpleName().toString();
            ClassName className = ClassName.bestGuess(classNameStr);
            TypeSpec.Builder classBuilder = TypeSpec.classBuilder(classNameStr + "_Pool")
                    .addModifiers(Modifier.FINAL, Modifier.PUBLIC);

            Boolean multiple = multiples.contains(enCLoseElement);

            MethodSpec.Builder pushMethodBuilder = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(className, "target");
            for (Element element : fieldElement) {
                String tag = element.getAnnotation(Push.class).tag();
                String fieldName = element.getSimpleName().toString();
                if (tag.isEmpty()) {
                    tag = fieldName;
                }
                TypeName field = ClassName.get(element.asType());
                String type = field.toString();
                int start = type.indexOf("<");
                int end = type.indexOf(">");
                String typePackage = "";
                String typeName = "";
                if (start < end) {
                    int typePackageEnd = type.lastIndexOf(".");
                    typePackage = type.substring(start + 1, typePackageEnd);
                    typeName = type.substring(typePackageEnd + 1, end);
                }
                if (typePackage.isEmpty() || typeName.isEmpty()) {
                    throw new NullPointerException(className.toString() + fieldElement.toString() + "无法获取泛型类型" + field);
                }
                StringBuilder data = new StringBuilder();
                Set<Modifier> modifiers = element.getModifiers();


                if (modifiers.contains(Modifier.PUBLIC)
                        || modifiers.isEmpty()) {
                    data.append("target.").append(fieldName);
                } else {
                    data.append("target.").append(getGetterMethodName(fieldName)).append("()");
                }

                ClassName typeClass = ClassName.get(typePackage, typeName);

                pushMethodBuilder.addStatement("$T.pool().push(target,$L.class, $L, $T.class, $S, $L)", liveDataPool, className,
                        data.toString(), typeClass, tag, multiple);

            }


            classBuilder.addMethod(pushMethodBuilder.build());
            classBuilder.build();

            String packageName = mElementUtils.getPackageOf(enCLoseElement).getQualifiedName().toString();
            try {
                JavaFile.builder(packageName, classBuilder.build())
                        .addFileComment("wghcwc自动生成")
                        .build().writeTo(mFiler);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }


        return false;
    }


    public String getGetterMethodName(String fieldName) {

        char[] chars = fieldName.toCharArray();
        if (chars[0] >= 'a' && chars[0] <= 'z') {
            chars[0] = (char) (chars[0] - 32);
        }
        return "get" + new String(chars);
    }
}

