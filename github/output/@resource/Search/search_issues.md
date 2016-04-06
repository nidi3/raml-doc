## Search issues

Find issues by state and keyword. (This method returns up to 100 results per page.)

The `q` search term can also contain any combination of the supported issue search qualifiers as described by the in-browser issue search documentation and search syntax documentation:

- `type` With this qualifier you can restrict the search to issues (issue) or pull request (pr) only.
- `in` Qualifies which fields are searched. With this qualifier you can restrict the search to just the title (title), body (body), comments (comment), or any combination of these.
- `author` Finds issues or pull requests created by a certain user.
- `assignee` Finds issues or pull requests that are assigned to a certain user.
- `mentions` Finds issues or pull requests that mention a certain user.
- `commenter` Finds issues or pull requests that a certain user commented on.
- `involves` Finds issues or pull requests that were either created by a certain user, assigned to that user, mention that user, or were commented on by that user.
- `team` For organizations you’re a member of, finds issues or pull requests that @mention a team within the organization.
- `state` Filter issues or pull requests based on whether they’re open or closed.
- `labels` Filters issues or pull requests based on their labels.
- `no` Filters items missing certain metadata, such as label, milestone, or assignee
- `language` Searches for issues or pull requests within repositories that match a certain language.
- `is` Searches for items within repositories that match a certain state, such as open, closed, or merged
- `created` or updated Filters issues or pull requests based on date of creation, or when they were last updated.
- `merged` Filters pull requests based on the date when they were merged.
- `status` Filters pull requests based on the commit status.
- `head` or base Filters pull requests based on the branch that they came from or that they are modifying.
- `closed` Filters issues or pull requests based on the date when they were closed.
- `comments` Filters issues or pull requests based on the quantity of comments.
- `user` or repo Limits searches to a specific user or repository.

If you know the specific SHA hash of a commit, you can use also use it to search for pull requests that contain that SHA. 
Note that the SHA syntax must be at least seven characters.