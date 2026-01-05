# Twow event Calendar

[wow-events](https://hazmimosbah.com/wow-events/)

<img src="https://i.imgur.com/9qRZtZY.png">
This's to help me and others on Twow to easily check raid reset times and pvp weekends.
It started with using [mustache](https://mustache.github.io/) to transform json request responses into [htmx](https://github.com/bigskysoftware/htmx) via a client-side template before it is swapped into the DOM.
And hosted on Render with a docker container while using Supabase's Postgres.

But now it's fully spring boot backend with angular [front](https://github.com/h4zm1/NgEverlook-Calendar).
