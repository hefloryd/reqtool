# ReqTool

ReqTool is a **model** and a **requirements editor** for the Eclipse [Capra][capra_home] framework. ReqTool is developed by [rt-labs][rt_home] in cooperation with a [research group][capra_gu] at the software engineering deportment at University of Gothenburg. It is partly funded by Vinnova within the [AMALTHEA4public][vinnova] project.

Capra is a traceability framework which enables applications to create and manage links between different forms of artifacts in Eclipse. ReqTool provides a concrete Capra model in the form of *specification files*. ReqTool specification files are XML files that are stored in Eclipse workspaces. They most importantly contain lists of requirements.

The following is a screen shot of the requirements editor and the requirements tree view.

![Requirements table screenshot][req_table_img] 

## Motivation

ReqTool demonstrate the capabilities of the Capra framework and the benefits of integrating a requirements tool with Capra and the Eclipse framework.

By using these frameworks the ReqTool editor can work with a wide variety of different kinds of artifacts, such as source code elements, Microsoft Word documents and test cases.

This can give software developers a convenient way to manage requirement, their links to other artifacts, and the status of tests for them. The convenience and overview that this provides can result in requirements with a higher quality and better control of the development process and the resulting software.

## Functionality

### Basic GUI and functionality of the ReqTool editor

- **Create** and **edit** requirements in a table editor.
- **Create links** between artifacts and requirements by dragging and dropping Eclipse resources and other elements to the requirements editor. 
    
### Advanced features of the ReqTool editor

- **Visualisation** of the requirement tree
- **Validation and syntax highlighting** of [Gherkin][gherkin] style requirements and user story requirements
    - Uses Gherkin parser from the [Cucumber project][cucumber].
- **Generation of [Robot Framework][robot_framework] tests** from Gherkin requirements
    - Uses Gherkin parser from the Cucumber project.
- **Generation of specification documents** in the [Markdown][markdown] file format
    - The generated files can easily be used to generated PDF or HTML documents with any of a large number of third-party tools.

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
