Phosphor: Dynamic Taint Tracking for the JVM
========


Phosphor is a system for performing dynamic taint analysis in the JVM, on commodity JVMs (e.g. Oracle's HotSpot or OpenJDK's IcedTea). This repository contains the source for Phosphor. For more information about how Phosphor works and what it could be useful for, please refer to our [Technical Report](https://mice.cs.columbia.edu/getTechreport.php?techreportID=1569) or email [Jonathan Bell](mailto:jbell@cs.columbia.edu).

Running
-------
Phosphor works by modifying your application's bytecode to perform data flow tracking. To be complete, Phosphor also modifies the bytecode of JRE-provided classes, too. The first step to using Phosphor is generating an instrumented version of your runtime environment. We have tested Phosphor with versions 7 and 8 of both Oracle's HotSpot JVM and OpenJDK's IcedTea JVM.

We'll assume that in all of the code examples below, we're in the same directory (which has a copy of [phosphor.jar](https://github.com/Programming-Systems-Lab/phosphor/raw/master/phosphor.jar)), and that the JRE is located here: `/Library/Java/JavaVirtualMachines/jdk1.7.0_45.jdk/Contents/Home/jre` (modify this path in the commands below to match your environment).

Then, to instrument the JRE we'll run:
`java -jar phosphor.jar /Library/Java/JavaVirtualMachines/jdk1.7.0_45.jdk/Contents/Home/jre jre-inst`

The instrumenter takes two primary arguments: first a path containing the classes to instrument, and then a destination for the instrumented classes. Optionally, you can specify additional classpath dependencies referenced from your code (may or may not be necessary, and explained in further below).

The next step is to instrument the code which you would like to track. We'll start off by instrumenting the demo suite provided under the PhosphorTests project. This suite includes a slightly modified version of [DroidBench](http://sseblog.ec-spride.de/tools/droidbench/), a test suite that simulates application data leaks. We'll instrument the [phosphortests.jar](https://github.com/Programming-Systems-Lab/phosphor/raw/master/phosphortests.jar) file:
`java -jar phosphor.jar phosphortests.jar inst`

This will create the folder inst, and place in it the instrumented version of the demo suite jar.

We can now run the instrumented demo suite using our instrumented JRE, as such:
`JAVA_HOME=jre-inst/ $JAVA_HOME/bin/java  -Xbootclasspath/a:phosphor.jar -cp inst/phosphortests.jar -ea phosphor.test.DroidBenchTest`
The result should be a list of test cases, with assertion errors for each "testImplicitFlow" test case.

Interacting with Phosphor
-----
Phosphor exposes a simple API to allow you to mark data with tags, and to retrieve those tags. The class ``edu.columbia.cs.psl.phosphor.runtime.Tainter`` contains all relevant methods (ignore the methods ending with the suffix $$INVIVO_PC, they are used internally), namely, getTaint(...) and taintedX(...) (with one X for each data type: taintedByte, taintedBoolean, etc).

We suggest that when you instrument your project, you pass any directories containing classes to the Phosphor instrumenter so that it can [properly resolve class hierarchies](http://chrononsystems.com/blog/java-7-design-flaw-leads-to-huge-backward-step-for-the-jvm). For example, if we were instrumenting our "PhosphorTest" folder, we would pass the "PhosphorTest/bin" as an additional argument to our instrumenter.

Support for Android
----
We have preliminary results that show that we can apply Phosphor to Android devices running the Dalvik VM, by instrumenting all Android libraries prior to dex'ing them, then dex'ing them, and deploying to the device. Note that we have only evaluated this process with several small benchmarks, and the process for using Phosphor on the Dalvik VM is far from streamlined. It is not suggested that the faint of heart try this.

In principle, we could pull .dex files off of a device, de-dex, instrument, and re-dex, but at time of writing, no de-dex'ing tool is sufficiently complete (for example, [dex2jar does not currently support the int-to char opcode](https://code.google.com/p/dex2jar/issues/detail?id=214&can=1&q=i2c)). Therefore, to use Phosphor on an Android device, you will need to [compile the Android OS](https://source.android.com) yourself, so that you get the intermediate .class files to instrument.

Once you have compiled Android, the next step will be to instrument all of the core framework libraries. For each library *X*, the classses are found within the file `out/target/common/obj/JAVA_LIBRARIES/X_intermediates/classes.jar`. Copy each of these jar's to a temporary directory, renaming them to *X.jar*. Then instrument each of these jar's following the instructions from the section above, *Running*. Then, use the `dx` tool provided with the Android SDK to convert each jar to dex:
`dx -JXmx3g --dex --core-library --output=dex/X.jar instrumented-jars/X.jar`. For the file, *framework.jar*, you will find that the jar now contains too many classes. Use the flag `--multi-dex` here, and assemble all of the output classes.dex files into a single jar.  

Next, use `dx` to also convert phosphor.jar to a dex file, and then copy all of the completed .dex files to the Android device. Use the same procedure to instrument each application. Then, using an `adb shell`, change the boot classpath to point to the instrumented core libraries, and invoke your application using the `dalvikvm` command.


For more information on applying Phosphor to Dalvik, please contact us.

Questions, concerns, comments
----
Please email [Jonathan Bell](mailto:jbell@cs.columbia.edu) with any feedback. This project is still under heavy development, and we are working on many extensions (for example, tagging data with arbitrary types of tags, rather than just integer tags), and would very much welcome any feedback.

License
-------
This software is released under the MIT license.

Copyright (c) 2013, by The Trustees of Columbia University in the City of New York.

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

Acknowledgements
--------
This project makes use of the following libraries:
* [ASM](http://asm.ow2.org/license.html), (c) 2000-2011 INRIA, France Telecom, [license](http://asm.ow2.org/license.html)

The authors of this software are [Jonathan Bell](http://jonbell.net) and [Gail Kaiser](http://www.cs.columbia.edu/~kaiser/). The authors are members of the [Programming Systems Laboratory](http://www.psl.cs.columbia.edu/), funded in part by NSF CCF-1161079, NSF CNS-0905246, and NIH U54 CA121852.

[![githalytics.com alpha](https://cruel-carlota.pagodabox.com/ae2f03ebde27be607b8ffe5a9911293d "githalytics.com")](http://githalytics.com/Programming-Systems-Lab/phosphor)
