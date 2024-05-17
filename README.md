# Observer API

Observer API is a RESTful service that provides access to repository details of GitHub users.

## Overview

The main functionality of the application is to list all repositories of a given GitHub username which are not forks. The API provides:

- Repository Name
- Owner Login
- For each branch: its name and the last commit SHA

Additionally, it handles specific error scenarios such as a non-existent GitHub user and unsupported `Accept` headers in requests.

## Table of Contents

- [Getting Started](#getting-started)
- [Usage](#usage)
- [Components](#components)
- [Error Handling](#error-handling)
- [Testing](#testing)
- [License](#license)

## Getting Started

### Prerequisites

- Java 21
- Spring Boot 3
- Maven (for building and running)

### Building

To build the project, navigate to the project directory and execute:

```bash
mvn clean install
```

### Running
To run the project after building, execute:
```bash
docker-compose up
```

## Usage
To fetch repositories for a specific GitHub user:
```bash
GET /repositories/{username}
Accept: application/json
```

Replace `{username}` with the desired GitHub username.

## **Components**

### Controllers

- `ObserverController`: The main controller handling incoming HTTP requests.

### Services

- `ObserverService`: Responsible for fetching data from the GitHub API.

### Models

- `Branch`: Represents a branch in a GitHub repository.
- `BranchDetails`: Provides details of a branch.
- `Commit`: Represents a commit in a GitHub repository.
- `Owner`: Represents the owner of a GitHub repository.
- `RepositoryDetails`: Represents detailed information about a repository.
- `Repository`: Represents a GitHub repository.

### Exceptions

- `UserNotFoundException`: Thrown when a specified GitHub user is not found.

## **Error Handling**

Errors are returned in the following format:

```json
{
    "status": "<HTTP_STATUS_CODE>",
    "message": "<ERROR_MESSAGE>"
}
```

For example, when a user tries to fetch data for a non-existent GitHub user:

```json
{
  "status": 404,
  "message": "User not found"
}
```

If a request comes with an Accept header value of application/xml, a 406 response is returned indicating "Only JSON requests are accepted".

### Testing

The application includes integration tests to validate its functionality against the acceptance criteria. Refer to ObserverIntegrationTest for details.

### License

This project is open-source. Feel free to fork, modify, and use as needed. Before using for commercial purposes, it's recommended to review any licensing constraints.

