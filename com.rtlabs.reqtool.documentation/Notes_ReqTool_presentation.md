
ReqTool Presentation
====================

1. Architecture
    - Artifact and trace model for requirements
        - A Specification object 
            - Is Trace Model
            - Has Artifact Model)
            - Contains Requirements objects
            - Requirements have child requirements and artifacts  

    - Model opened in an editor
        - A user specified model file is used  
        - Enables the use of multiple models
        - This pattern could be useful in the rest of Capra

2. Basic GUI and functionality
    - Display and edit requirements
    - Link artifacts to requirements
    
3. Advanced features
    - Visualise requirement tree
    - Validate and syntax highlight Gherkin requirements and user story requirements
        - Uses Gherkin parser from the Cucumber project
    - Generate Robot Framework tests from Gherkin requirements
        - Uses Gherkin parser from the Cucumber project
    - Generate Specification documents in Markdown

4. Future work:
    - Visualise test results in the requirements table
    - Export/import ReqIF
    - GUI work: Better editing and navigation, fix bugs

