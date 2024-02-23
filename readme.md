# Prototype app for showcasing GiantBomb API search

Try it here: https://unabomber-1.onrender.com

Note that it's deployed on a free tier, so it might load extremely slowly (be patient, it's unlikely that it's broken, it just takes too long to load) and search too, will get super sluggish. I mean, you may have to wait for like two-three minutes for search results to appear.

## Running locally
### Required

- functional GiantBombAPI key. [Sign up and get the key here](https://www.giantbomb.com/api/)

### Using the Docker Image

1. clone the repo
    ```bash
    git clone https://github.com/agzam/unabomber
    cd unabomber
    ```

2. build the image

    ```bash
    sudo docker build -t unabomber .
    ```

3. run the app

    providing the API key: 

    ```bash
    sudo docker run -p 3000:3000 --name bomba unabomber \
    java -jar /unabomber.jar  \
    --api-key="YOUR-API-KEY"
    ```

    ```bash
    open localhost:3000
    ```

### Running/testing locally without Docker

#### Prerequisites

- clojure-cli, [installation instructions](https://clojure.org/guides/install_clojure)
- nodejs, either install it directly, via package manager, or use [nvm](https://github.com/nvm-sh/nvm)
- configured local gpg service (required for storing the API key)

##### Store API-KEY in an encrypted file

Run the following command while in the project directory, *supplement your GiantBombAPI key and the email used with your private gpg key*:

```bash
echo '{:giantbomb-api-key "YOUR-API-KEY"}' | \ 
    gpg --recipient YOUR@EMAIL.COM \
    --output ./resources/creds.gpg --encrypt
```

The app then uses that info while keeping your API key secured. **If that step is not done, the search won't work at all**.

##### Install required npm packages

```bash
npm install
```

#### Build and run 

```bash
clojure -T:build uberjar
```

This would build the standalone jar with minified JavaScript.

Then you can run it locally:

```bash
java -jar ./target/unabomber.jar 
```

The standalone jar would run the server on port 3000

```bash
open localhost:3000
```

### To study the code, the best is to run the app in the REPL

Instead of running the standalone "production-ready" app, you may choose to run the "dev-grade" app (that lacks all optimizations for faster feedback loop) in the REPL. That is the best option to navigate through the code and to understand how things are stitched together. In general, exploring any kind of Lisp code while having a connected REPL session is a much more delightful experience.

Please avoid running both options (the "prod" jar and the "dev" REPL) at the same time. While theoretically they should not conflict, I have not tested this scenario, so some unexpected issues could arise.

```bash
clojure -M:dev:backend:frontend 
```

That should open the REPL. Once in there:

```clojure
user> (go)
```

That will kick off the "assembling the system" process using Integrant. Then the server would be available on port 3003. Or whatever is configured in `/dev/config.edn`

```bash
open localhost:3003
```

**Remember, prod version and dev version use different ports**, so they can run without conflicting with each other. Still, it's probably best not to run both at the same time.

#### Emacs & CIDER 

It is, of course possible to REPL with VSCode and IntelliJ and Vim, etc., but I only did this in Emacs.

- Default option is to run a single REPL. You can simply run `cider-jack-in` and type `(go)` in the REPL. All required elisp vars would be initialized with proper values, see: `./.dir-locals.el`

- Or, you may like to run two simultaneous REPLs - for both, Clojure and Clojurescript. Then evaling Clojure code would go to clj REPL and Clojurescript code would got into the browser REPL. Simply, run `M-x cider-jack-in-clj&cljs`, and type `(go)` inside the Clojure REPL.

  That should start `shadow-cljs` compilation and `postcss` watchers, they'd be running in the sibling Clojurescript REPL. Once you open the app in the browser, REPL would automatically connect to it. Try typing something like `(js/alert)` - the dialog in the browser would pop-up, proving that cljs REPL is connected.

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

used for routing, both on front and back-end. The simplest way for newcomers to go through the code is probably to start at the entry point for the routes. On front-end that would be in `frontend/routing.cljs`, server routes are in `backend/system.clj`. There's tons of boilerplate in the `unabomber.backend.system` namespace, specifically in the middleware section. Most of it can probably be removed, I just decided not to waste time fixing what's not broken, try not to get too caught up in it, it's not super important. 

### integrant
https://github.com/weavejester/integrant

is used for "assembling the system", it's useful to reset the server when you make changes to the backend code, without having to restart the whole thing. Simply run `(reset)`, in the Clojure REPL.

## Testing

There is a basic framework for testing. Backend test are pretty straightforward. Front-end tests are for now only cover unit testing and run with [Karma](https://karma-runner.github.io/latest/index.html). I'm hoping to add some end-to-end tests with Cypress at some point. 

The best way to start examining tests and how they pieced together, is to start at GitHub Actions workflow file - `.github/workflows/unabomber.yml`
