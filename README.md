Storm/Finagle Hackathon Submission - Poursquare 
===============================================

This was my submission to the Storm/Finagle Hackathon which took place at hack/reduce 
in July of 2013.  

This repo contains the code for the Storm portion of the application.  This includes all the 
spouts, bolts, and topology that were used on the Storm cluster.

Getting Started
---------------

### Prerequisites

The first thing you need to do is get access to the [Twitter API](https://dev.twitter.com/).  Once you are 
set up, you need to retrieve a bearer token from Twitter so that you can access
the [search API](https://dev.twitter.com/docs/api/1.1/get/search/tweets).  The search API uses
application-only authentication and instructions on how to get a token can be found 
[here](https://dev.twitter.com/docs/auth/application-only-auth).  I used Finagle to do this but you can 
just as easily do it with curl or any HTTP client.

You will also need MongoDB installed.  If you are on Linux (or OSX), you can do this through whichever 
package manager/software updater your distro supports.  On Windows, just follow the 
instructions [here](http://docs.mongodb.org/manual/tutorial/install-mongodb-on-windows/).    

Install the [Java SDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html) 
and [Maven](http://maven.apache.org/download.cgi).

The last thing you will need is git.  I would assume most of you have it already, but if not, you can 
grab it through your package manager or go [here](http://git-scm.com/download/win) for Windows.  Then 
check out the docs on GitHub on how to clone a repo.  

Although I am using Scala, if you all you want to do is run the topology, you do not have to have Scala 
installed.  If you are using Eclipse, you can either download the Scala IDE (which I recommended) or install 
the Scala plugin.  I did have to create a separate library entry for 2.10.2 since I couldn't 
find out how to update Scala IDE to the latest version of 2.10.


### Installing

First, clone the repo onto your local machine.

I am using Maven and not sbt, so if you are using Eclipse, you can just import as a Maven project.  The 
import into IntelliJ should be pretty straightforward as well (although I am not certain on how 
to do this). You can also just run Maven from the command line.

The next step is to make an environmental variable called TWITTER_BEARER_TOKEN and set the value to 
your Twitter API bearer token.  I did  this in my run configuration in Eclipse but if you are running 
from the command line you can set it there.  

### Running

If you are in Eclipse, create a run configuration for a Scala Application that targets the 
`com.infinitemule.hopperhack.storm.PoursquareTopology` class.  Don't forget specify the bearer token 
environmental variable.

If you are on the command line, you should be able to run `mvn compile` and then: 

  mvn exec:java -Dexec.mainClass=com.infinitemule.hopperhack.storm.PoursquareTopology  

The topology will run in local mode for two minutes.  This is hard coded in the class, so you will have to edit the file 
if you wanted it to run longer.


Notes
-----
* I am not a Storm expert.  I started learning Storm a few days before the hackathon.  If you see 
something that can be improved, please let me know.
* I didn't do any adjustment to the topology.  Everything has a three for a parallelism hint because 
that's what I used when I was testing.  My guess would be that you would look at the metrics that
the Storm UI is collecting and make appropriate adjustments to those numbers.
* I had a hard time finding the proper way of feeding a spout.  I based mine off of the Storm Kestral spout.
* I also had a hard time finding a good example of how to use Finagle within a Storm bolt.  If you know of 
a better way of doing this, please let me know.  