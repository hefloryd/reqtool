# ReqTool

ReqTool is a **model** and a **requirements editor** for the Eclipse [Capra][capra_home] framework. ReqTool is developed by [rt-labs][rt_home] in cooperation with a [research group][capra_gu] at the software engineering deportment at University of Gothenburg. It is partly funded by Vinnova within the [AMALTHEA4public][vinnova] project.

Capra is a traceability framework which enables applications to create and manage links between different forms of artifacts in Eclipse. ReqTool provides a concrete Capra model in the form of *specification files*. ReqTool specification files are XML files that are stored in Eclipse workspaces. They most importantly contain lists of requirements.

The following is a screen shot of the requirements editor and the requirements tree view.

![Requirements table screenshot][req_table_img] 

## Motivation

ReqTool demonstrate the capabilities of the Capra framework and the benefits of integrating a requirements tool with Capra and the Eclipse framework.

By using these frameworks the ReqTool editor can work with a wide variety of different kinds of artifacts, such as source code elements, Microsoft Word documents and test cases.

This can give software developers a convenient way to manage requirement, their links to other artifacts, and the status of tests for them. The convenience and overview that this provides can result in requirements with a higher quality and better control of the development process and the resulting software.

ReqTool is created because it is valuable with a simple, lightweight tool that is designed from scratch to take advantage of the functionality that Capra provides.

## Functionality

### Basic GUI and functionality of the ReqTool editor

- **Create** and **edit** requirements in a table based editor.

- **Create links** between artifacts and requirements by dragging and dropping Eclipse resources and other elements to the requirements editor.

### Advanced features of the ReqTool editor

- **Visualisation** of the requirement tree in the *Trace View*.

- **Validation and syntax highlighting** of [Gherkin][gherkin] style requirements and user story style requirements
    - Uses Gherkin parser from the [Cucumber project][cucumber].
    - Active in requirements editor, in the body field of requirement with the "Gherkin" type. 

- **Generation of [Robot Framework][robot_framework] tests** from Gherkin requirements
    - Uses Gherkin parser from the Cucumber project.
    - Trigger command with a toolbar button or from context menu in the editor.

- **Generation of specification documents** in the [Markdown][markdown] file format
    - The generated files can easily be used to generated PDF or HTML documents with any of a large number of third-party tools.
    - Tricker with the *Export* -> *ReqTool* -> *Specification Document* command.

## Prerequisites and Dependencies

### Build Tools

Either:

* Maven
* Eclipse, with plug-in development tools and modeling tools installed

### Source Code Dependencies

ReqTool depends on the following software. If it doesn't exist in the Eclipse workspace it will be downloaded as part of the target platform resolution process in a normal build.

* Java 8
* The Eclipse Neon platform
* Eclipse Orbit
* The `org.eclipse.capra.core` Eclipse bundle version 0.7.0. TODO: These is yet no mechanism to use 
  an old version of Capra, except by checking out the source code and placing it in 
  the Eclipse workspace. 
* Eclipse Nebula NatTable
* TODO: ReqIF?

## Downloading, Building and Running

Obtain a copy of the source code by cloning the ReqTool repository with the following Git command:

    git clone <REPO_URL>

### Building

The source code can be built using either with Maven Tycho, or from within the Eclipse 
using the PDE and modeling tooling.

* To build *using Maven*, run the command `mvn verify` from inside the `com.rtlabs.reqtool.releng` directory.

* To build from *within Eclipse*, first set the target platform file in `com.rtlabs.reqtool.releng.target`. 
  The perform a normal build. 

### Running

* Run an Eclipse instance with the ReqTool bundles installed.

* Create a requirements model file using the *New* -> *ReqTool* -> *Specification* command. The file will open in the ReqTool requirements editor.

WARNING: The Capra `org.eclipse.capra.generic` bundles must not be installed in the Eclipse 
instance running ReqTool. That is because Capra (at least up to version 0.7.0) can not 
handle multiple models running at once.

## Future work

- Visualise **test results** in the requirements table.
    - The Eclipse [Mylyn][mylyn] tool is used to integrate with a large number of different kinds of test and continuous integration servers.
- Export/import **ReqIF**
- GUI work: Better editing and navigation, fix bugs

[req_table_img]: com.rtlabs.reqtool.documentation/Requirement_table_screenshot.png
[capra_home]: https://projects.eclipse.org/projects/modeling.capra
[rt_home]: http://rt-labs.com
[capra_gu]: http://medarbetarportalen.gu.se/staff/f--publikationskort/?publicationId=249696
[vinnova]: https://www.vinnova.se/p/amalthea4public/
[gherkin]: https://docs.cucumber.io/docs/gherkin.html
[cucumber]: https://cucumber.io/
[markdown]: https://en.wikipedia.org/wiki/Markdown
[mylyn]: https://www.eclipse.org/mylyn/
[robot_framework]: http://robotframework.org/
