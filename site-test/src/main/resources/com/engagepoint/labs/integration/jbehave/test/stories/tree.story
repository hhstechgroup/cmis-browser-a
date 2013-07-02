Meta:

Narrative:
As a user
I want to perform an action
So that I can achieve a business goal

Scenario: User goes to the Home page

Given user is on Home page
Then Find title Hallo user


Scenario: User clicks tree root

Given user is on Home page
When user clicks tree root
Then root is selected

Scenario: User expands tree root

Given user is on Home page
When user clicks root expand
Then root is expanded