# Github Commenter
A tiny commandline application that allows you to write comments on Github pull requests.

A example where this tool is useful is when used with Continuous Integration (CI) and code quality checks like static code analysis. 
With Github Commenter you can post the result of such code quality checks as a comment directly on the pull request. 
Apart from simple comments (displayed in the "conversation" section on a github pull request) this tool can also post comments 
on a certain file with a given line number. 
Example: Let's say that a static code analysis tool has detected an error in File `com/example/Foo.java` on `Line 86`
then with this tool you can directly post a code review comment on that given file and line (as any other human code reviewer would do).

# Installation
The application is just a tiny java `.jar` executable. So you basically just need a jvm to run this tool.
The latest version of the application can be found [here]().

## Best parctice on CI
We consider it as best practice to put this `.jar` executable in your git repository and execute it from command line on CI as needed.

# Usage
```
  java -jar .ci/githubcommentor.jar -file path/to/comments.xml -owner YourGithubUsername -repository YourRepositoryName -pullrequest ThePullRequestId -sha ShaOfPullRequest -accesstoken AccessToken
```

## Input
The comment or comments (as you can post arbitarry many comments at once) must be written in a structured xml file that looks like this:

```xml
<comments>
    <!-- A simple comment -->
    <comment>
        This is a simple Comment posted on the Conversation section of a pull request
    </comment>
    
    <!-- A code review comment on a certain file on a given line number -->
    <codelinecomment
        filePath="src/com/example/Foo.java"
        lineNumber="86">
        Null check is missing!
    </codelinecomment>
    
     <!-- You can write multiple comments -->
     <comment>
        Yet another simple Comment posted on the Conversation section of a pull request.
        Oh and btw. You **can also use markdown**
     </comment>
     
     <!-- You can write multiple code line comments too -->
    <codelinecomment
      filePath="src/com/example/Bar.java"
      lineNumber="42">
        <![CDATA[ You can also use CDATA ]]>
    </codelinecomment>
   
</comments>
```

# Permissions
The `AccessToken` needs admin permission on Repo.