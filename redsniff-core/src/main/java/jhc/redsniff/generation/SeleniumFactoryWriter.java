/*******************************************************************************
 * Copyright 2014 JHC Systems Limited
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
 *******************************************************************************/
package jhc.redsniff.generation;

import static com.google.common.base.Joiner.on;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.StringTokenizer;

import org.hamcrest.generator.FactoryMethod;
import org.hamcrest.generator.FactoryWriter;


public class SeleniumFactoryWriter implements FactoryWriter {

    private final PrintWriter output;
    private final String javaPackageName;
    private final String javaClassName;

    private String indentationString = "  ";
    private String newLine = "\n";

    private int indentation = 1;


	public SeleniumFactoryWriter(String javaPackageName, String javaClassName,	Writer output) {
		this.javaPackageName = javaPackageName;
	    this.javaClassName = javaClassName;
	    this.output = new PrintWriter(output);
	}
	
    @Override
    public void writeHeader() throws IOException {
        output.append("// Generated source.").append(newLine)
                .append("package ").append(javaPackageName).append(';').append(newLine).append(newLine);
        output.append("public class ").append(javaClassName).append(" {").append(newLine).append(newLine);
    }

    @Override
    public void writeFooter() throws IOException {
        output.append('}').append(newLine);
    }

    @Override
    public void close() throws IOException {
        output.close();
    }

    @Override
    public void flush() throws IOException {
        output.flush();
    }

    @Override
    public void writeMethod(String generatedMethodName, FactoryMethod factoryMethodToDelegateTo)
            throws IOException {
        writeJavaDoc(factoryMethodToDelegateTo);
        indent();
        output.append("public static ");
        writeGenericTypeParameters(factoryMethodToDelegateTo);
        output.append(factoryMethodToDelegateTo.getReturnType());
        if (factoryMethodToDelegateTo.getGenerifiedType() != null) {
        	
            output.append('<').append(generifiedTypesList(factoryMethodToDelegateTo)).append('>');
        }
        output.append(' ').append(generatedMethodName);
        writeParameters(factoryMethodToDelegateTo);
        writeExceptions(factoryMethodToDelegateTo);
        output.append(" {").append(newLine);
        indentation++;
        writeMethodBody(factoryMethodToDelegateTo);
        indentation--;
        indent();
        output.append('}').append(newLine).append(newLine);
    }

	private String generifiedTypesList(FactoryMethod factoryMethodToDelegateTo) {
		if(factoryMethodToDelegateTo instanceof FactoryMethodPermittingMultipleGenericTypes) 
			return on(", ").join(((FactoryMethodPermittingMultipleGenericTypes) factoryMethodToDelegateTo)
									.getGenerifiedTypes());
		
		else
			return factoryMethodToDelegateTo.getGenerifiedType();
	}

    private void writeGenericTypeParameters(FactoryMethod factoryMethod) {
        if (!factoryMethod.getGenericTypeParameters().isEmpty()) {
            output.append('<');
            boolean seenFirst = false;
            for (String type : factoryMethod.getGenericTypeParameters()) {
                if (seenFirst) {
                    output.append(", ");
                } else {
                    seenFirst = true;
                }
                output.append(type);
            }
            output.append("> ");
        }
    }

    private void writeMethodBody(FactoryMethod factoryMethod) {
        indent();
        output.append("return ").append(factoryMethod.getMatcherClass());
        output.append('.');

        // lets write the generic types
        if (!factoryMethod.getGenericTypeParameters().isEmpty()) {
            output.append('<');
            boolean seenFirst = false;
            for (String type : factoryMethod.getGenericTypeParameters()) {
                if (seenFirst) {
                    output.append(",");
                } else {
                    seenFirst = true;
                }
                // lets only print the first word of the type
                // so if its 'T extends Cheese' we just print T
                //output.append(type);

                StringTokenizer iter = new StringTokenizer(type);
                iter.hasMoreElements();
                output.append(iter.nextToken());
            }
            output.append(">");
        }

        output.append(factoryMethod.getName());
        output.append('(');
        boolean seenFirst = false;
        for (FactoryMethod.Parameter parameter : factoryMethod.getParameters()) {
            if (seenFirst) {
                output.append(", ");
            } else {
                seenFirst = true;
            }
            output.append(parameter.getName());
        }
        output.append(')');
        output.append(';').append(newLine);
    }

    private void writeExceptions(FactoryMethod factoryMethod) {
        boolean seenFirst = false;
        for (String exception : factoryMethod.getExceptions()) {
            if (seenFirst) {
                output.append(", ");
            } else {
                output.append(" throws ");
                seenFirst = true;
            }
            output.append(exception);
        }
    }

    private void writeParameters(FactoryMethod factoryMethod) {
        output.append('(');
        boolean seenFirst = false;
        for (FactoryMethod.Parameter parameter : factoryMethod.getParameters()) {
            if (seenFirst) {
                output.append(", ");
            } else {
                seenFirst = true;
            }
            output.append(parameter.getType()).append(' ').append(parameter.getName());
        }
        output.append(')');
    }

    private void writeJavaDoc(FactoryMethod factoryMethod) {
        if (factoryMethod.getJavaDoc() != null) {
            String[] lines = factoryMethod.getJavaDoc().split("\n");
            if (lines.length > 0) {
                indent();
                output.append("/**").append(newLine);
                for (String line : lines) {
                    indent();
                    output.append(" * ").append(line).append(newLine);
                }
                indent();
                output.append(" */").append(newLine);
            }
        }
    }

    private void indent() {
        for (int i = 0; i < indentation; i++) {
            output.append(indentationString);
        }
    }
	

}
