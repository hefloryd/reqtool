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

- **Visualise test results** in the requirements table
    - A context menu triggered command fetches test results from a build server, matches them against the artifacts linked in the table, and decorates the table to indicate test results.
    - The build server is configured in the Mylyn *Builds* view.
    - The Eclipse [Mylyn][mylyn] API is to fetch test results.

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
* The `org.eclipse.capra.core` Eclipse bundle.
* To link requirements to artifact the Capra handlers are needed.
* Eclipse Nebula NatTable

The Capra packages are included in the ReqTool update site.

## Downloading, Building and Running

### Installing pre-built version in existing Eclipse

An update site file is [availible for download][site].

### Downloading source code

Obtain a copy of the source code by cloning the ReqTool repository with the following Git command:

    git clone <REPO_URL>

### Building

The source code can be built using either with Maven Tycho, or from within the Eclipse 
using the PDE and modeling tooling.

* To build *using Maven*, run the command `mvn verify` from inside the `com.rtlabs.reqtool.releng` directory.

* To build from *within Eclipse*, first set the target platform file in `com.rtlabs.reqtool.releng.target`. 
  The perform a normal build.

#### Capra as a Requirement

The needed Capra bundles are downloaded automatically as a part of the target platform.

If that fails, or to work with a modified version of Capra, its source code repository can be cloned, and its projects imported into the Eclipse workspace.

### Running

* Run an Eclipse instance with the ReqTool bundles installed.

* Create a requirements model file using the *New* -> *ReqTool* -> *Specification* command. The file will open in the ReqTool requirements editor.

* To link an artifact with a requirement, simply drag-and-drop the artifact on the requirement in the ReqTool requirements editor.

WARNING: The Capra `org.eclipse.capra.generic` bundles must not be installed in the Eclipse 
instance running ReqTool. That is because Capra (at least up to version 0.7.0) can not 
handle multiple models running at once.

## Future work

### Features

- Export/import **ReqIF**
- GUI work: Better editing and navigation, fix bugs

### Bugs, Defects and Limitations
- It is not possible to input newlines in the requirements body editor. The entry key commits the current text.
- It is currently not possible to remove linked artifacs from requirements (its fairly easy to edit the XML though :) ).
- Many other GUI and usability features...

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
[site]: https://github.com/hefloryd/reqtool/releases
