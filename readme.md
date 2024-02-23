# Prototype app for showcasing GiantBomb API search

## Prerequisites for running/testing locally

- clojure-cli, [installation instructions](https://clojure.org/guides/install_clojure)
- nodejs, either install it directly, via package manager, or use [nvm](https://github.com/nvm-sh/nvm)
- functional GiantBombAPI key. [Sign up and get the key here](https://www.giantbomb.com/api/)
- configured local gpg service (required for storing the API key)

### Right after cloning the repo

Run the following command while in the project directory, *supplement your GiantBombAPI key and the email used with your private gpg key*:

```bash
$> echo '{:giantbomb-api-key "YOUR-API-KEY"}' | gpg --recipient YOUR@EMAIL.COM --output ./resources/creds.gpg --encrypt
```

#### Install required npm packages

```bash
$> npm install
```

## To build and run locally

```bash
$> clojure -T:build uberjar
```

This would build the standalone jar with minified JavaScript.

Then you can run it locally:

```bash
$> java -jar ./target/unabomber.jar 
```

The standalone jar would run the server on port 3000

```bash
$> open localhost:3000
```

## To study the code, the best is to run the app in the REPL

```bash
clojure -M:dev:backend:frontend 
```

That should open the REPL. Once in there:

```clojure
user> (go)
```

That will kick off the "assembling the system" process using Integrant. Then the server would be available on port 3003. Or whatever is configured in `/dev/config.edn`


```bash
$> open localhost:3000
```

### Emacs & CIDER 

- You may choose to run a single REPL. You can simply run `cider-jack-in` and type `(go)` in the REPL. All required elisp vars would be initialized with proper values, see: `./.dir-locals.el`

- Or, you may like to run two simultaneous REPLs - for both, Clojure and Clojurescript. Run `M-x cider-jack-in-clj&cljs`, and type `(go)` inside the Clojure REPL.

  There would be `shadow-cljs` compilation and `postcss` watcher processes running in the second, sibling Clojurescript REPL. Once you open the app in the browser, REPL would automatically connect to it. Try typing something like `(js/alert)` - the dialog in the browser would pop-up, proving that cljs REPL is connected.

## Project components

### shadow-cljs
All front-end dependencies are in `./deps.edn` and `./package.json`

shadow-cljs setup pretty much follows the official guide; there's nothing unorthodox there, see: https://shadow-cljs.github.io/docs/UsersGuide.html


### tailwind CSS
https://tailwindcss.com/docs

Tailwind CSS class names get collected via `postcss` build process, see `package.json -> scripts` section

### re-frame
https://day8.github.io/re-frame

The project's structure follows general conventions for re-frame projects. 

### reitit
https://github.com/metosin/reitit

used for routing, both on front and back-end. The simplest way for newcomers to go through the code is probably to start at the entry point for the routes. On front-end that would be in `frontend/routing.cljs`, server routes are in `backend/system.clj`

### integrant
https://github.com/weavejester/integrant

## Testing

There is basic framework for testing. Backend test are pretty straightforward. Front-end tests are for now only cover unit testing and run with [Karma](https://karma-runner.github.io/latest/index.html). I'm hoping to add some end-to-end tests with Cypress at some point. 

The best way to start examining tests is to start with GitHub Actions workflow file - `.github/workflows/unabomber.yml`
