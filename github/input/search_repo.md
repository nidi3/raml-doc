## Search repositories
Find repositories via various criteria. This method returns up to 100 results [per page](https://developer.github.com/v3/#pagination).

The `q` search term can also contain any combination of the supported repository search qualifiers as described by the in-browser repository search documentation and search syntax documentation:

- `in` Qualifies which fields are searched. With this qualifier you can restrict the search to just the repository name, description, readme, or any combination of these.
- `size` Finds repositories that match a certain size (in kilobytes).
- `forks` Filters repositories based on the number of forks.
- `fork` Filters whether forked repositories should be included (true) or only forked repositories should be returned (only).
- `created` or `pushed` Filters repositories based on date of creation, or when they were last updated.
- `user` or `repo` Limits searches to a specific user or repository.
- `language` Searches repositories based on the language they’re written in.
- `stars` Searches repositories based on the number of stars.
