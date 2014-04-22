Reveal.initialize({
	controls: true,
	progress: true,
	history: true,
	center: true,

	theme: Reveal.getQueryHash().theme, // available themes are in /css/theme
	transition: Reveal.getQueryHash().transition || 'default', // default/cube/page/concave/zoom/linear/fade/none

	dependencies: [
	  { src: 'bower_components/reveal.js/lib/js/classList.js', condition: function() { return !document.body.classList; } },
	  { src: 'bower_components/reveal.js/plugin/markdown/marked.js', condition: function() { return !!document.querySelector( '[data-markdown]' ); } },
	  { src: 'bower_components/reveal.js/plugin/markdown/markdown.js', condition: function() { return !!document.querySelector( '[data-markdown]' ); } },
	  { src: 'bower_components/reveal.js/plugin/highlight/highlight.js', async: true, callback: function() { hljs.initHighlightingOnLoad(); } },
	  { src: 'bower_components/reveal.js/plugin/zoom-js/zoom.js', async: true, condition: function() { return !!document.body.classList; } },
	  { src: 'bower_components/reveal.js/plugin/notes/notes.js', async: true, condition: function() { return !!document.body.classList; } }
	]
});

var Finagle = {};

Finagle.handleEvent = function(isSlideEvent) {

  var currentSlideId = Reveal.getCurrentSlide().id;
  var currentFragment = Reveal.getIndices().f;
  console.log(Reveal.getIndices())

  // Don't go any further if the slide has no ID (i.e. the string is empty).
  if (!currentSlideId) {
    return;
  }

  // If there is no entry corresponding to the current slide in the map, don't go further.
  var functions = Finagle.slideFunctions[currentSlideId];
  if (functions == null) {
    return;
  }

  // Call the init function when arriving on a slide for the first time.
  if (isSlideEvent) {
    var initFunction = functions.init;
    if (initFunction != null) {
      initFunction();
      // Make sure we don't call the init function again.
      functions.init = null;
    }
  }

  var fragmentFunction = functions[currentFragment];
  if (fragmentFunction != null) {
    fragmentFunction();
  }
}

Finagle.handleSlideEvent = function() {
  Finagle.handleEvent(true);
};

Finagle.handleFragmentEvent = function() {
  Finagle.handleEvent(false);
};

Reveal.addEventListener('slidechanged', Finagle.handleSlideEvent);

Reveal.addEventListener('fragmentshown', Finagle.handleFragmentEvent);

Reveal.addEventListener('fragmenthidden', Finagle.handleFragmentEvent);

function cluster(slide) {

  var newNode = function(name, id) {
  	return {id: name+id || name, name: name, children:[], index:id};
  }

  this.clear = function() {
  	root.children = [];
  	update();
  }

  this.setServices = function(services) {
  	root.children = [];
    for (var i in services) {
    	var children = setupPipeline(services[i], i);
		for(var j in children) root.children.push(children[j]);
    };
  	update();
  }

  var setupPipeline = function(pipe, index) {
  	if(Array.isArray(pipe[0])) {
  		var nodes = pipe[0];
  		var children = [];
  		for(var i in nodes) {
  			children.push(newNode(nodes[i], index));
  		}
  		return children;
  	}
  	var node = newNode(pipe[0], index);
    if(pipe.length > 1) {
    	var children = setupPipeline(pipe.slice(1), index);
    	for(var j in children) node.children.push(children[j]);
    }
  	return [node];
  }

  var findClient = function(id) {
    for (var i in root.children) {if (root.children[i].id === id) return root.children[i]};
  }

	var m = [20, 60, 20, 100],
	    w = 1000 - m[1] - m[3],
	    h = 750 - m[0] - m[2],
	    i = 0,
	    root;

	var tree = d3.layout.cluster()
	    .size([h-60, w-60]);

	var diagonal = d3.svg.diagonal()
	    .projection(function(d) { return [d.y, d.x]; });

	var vis = slide.append("svg:svg")
	    .attr("width", w + m[1] + m[3])
	    .attr("height", h + m[0] + m[2])
	  .append("svg:g")
	    .attr("transform", "translate(" + m[3] + "," + m[0] + ")");

	root = newNode("UberApp", 0);
	root.x0 = 0;
	root.y0 = h/2;

	var types = d3.svg.symbolTypes;

	var update = function() {

	  var duration = 500;

	  var nodes = tree.nodes(root).reverse();

	  nodes.forEach(function(d) { d.y = d.depth * 125; });

	  var node = vis.selectAll("g.node")
	      .data(nodes, function(d) { return d.id || (d.id = ++i); });

	  var nodeEnter = node.enter().append("svg:g")
	      .attr("class", "node")
	      .attr("transform", function(d) { return "translate(" + root.y0 + "," + root.x0 + ")"; })

      nodeEnter.append("path")
        .attr("class", function(d) { return d.parent ? types[d.index] : "root"; })
        .attr("d", d3.svg.symbol()
          .type(function(d) { return types[d.index]; })
        );
	  
	  nodeEnter.append("svg:text")
	      .attr("x", function(d) { return d.parent == null || d.children || d._children ? -10 : 10; })
	      .attr("dy", ".35em")
	      .attr("text-anchor", function(d) { return d.parent == null || d.children || d._children ? "end" : "start"; })
	      .text(function(d) { return d.name; })
	      .style("fill-opacity", 1e-6);

	  var nodeUpdate = node.transition()
	      .duration(duration)
	      .attr("transform", function(d) { return "translate(" + d.y + "," + d.x + ")"; });

	  nodeUpdate.select("circle")
	      .attr("r", 4.5)
	      .style("fill", function(d) { return d._children ? "lightsteelblue" : "#fff"; });

	  nodeUpdate.select("text")
	      .style("fill-opacity", 1);

	  var nodeExit = node.exit().transition()
	      .duration(duration)
	      .attr("transform", function(d) { return "translate(" + root.y + "," + root.x + ")"; })
	      .remove();

	  nodeExit.select("circle")
	      .attr("r", 1e-6);

	  nodeExit.select("text")
	      .style("fill-opacity", 1e-6);

	  var link = vis.selectAll("path.link")
	      .data(tree.links(nodes), function(d) { return d.target.id; });

	  link.enter().insert("svg:path", "g")
	      .attr("class", "link")
	      .attr("d", function(d) {
	        var o = {x: root.x0, y: root.y0};
	        return diagonal({source: o, target: o});
	      })
	    .transition()
	      .duration(duration)
	      .attr("d", diagonal);

	  link.transition()
	      .duration(duration)
	      .attr("d", diagonal);

	  link.exit().transition()
	      .duration(duration)
	      .attr("d", function(d) {
	        var o = {x: root.x, y: root.y};
	        return diagonal({source: o, target: o});
	      })
	      .remove();

	  nodes.forEach(function(d) {
	    d.x0 = d.x;
	    d.y0 = d.y;
	  });
	}
   
    update(root);

}

Finagle.apps = {};
Finagle.slideApp = function(id) {
	var app = Finagle.apps[id];
	if(!app) app = Finagle.apps[id] = new cluster(d3.select('#' + id));
	return app;
}

Finagle.slideFunctions = {
	'slide-a' : {
		'init': function() {
			
		},
		'-1': function() {
			Finagle.slideApp('slide-a').clear();
		},
		0: function() {
			Finagle.slideApp('slide-a').setServices([
				["JDBC", "MySQL"]
			]);
		},
		1: function() {
			Finagle.slideApp('slide-a').setServices([
				["JDBC", "MySQL"],
				["JRedis", "Redis"]
			]);
		},
		2: function() {
			Finagle.slideApp('slide-a').setServices([
				["JDBC", "MySQL"],
				["JRedis", "Redis"],
				["HTTP", "S3"]
			]);
		},
		3: function() {
			Finagle.slideApp('slide-a').setServices([
				["JDBC", "MySQL"],
				["JRedis", "Redis"],
				["HTTP", ["S3", "AwesomeService"]]
			]);
		},
		4: function() {
			Finagle.slideApp('slide-a').setServices([
				["JDBC", "MySQL"],
				["JRedis", "Redis"],
				["HTTP", ["S3", "AwesomeService"]],
				["Memcache", "Memcached"]
			]);
		},
		5: function() {
			Finagle.slideApp('slide-a').setServices([
				["JDBC", "MySQL"],
				["JRedis", "Redis"],
				["HTTP", ["S3", "AwesomeService"]],
				["Memcache", "Memcached"],
				["java-apns", "APNS"]
			]);
		},
		6: function() {
			Finagle.slideApp('slide-a').setServices([
				["AsyncShim", "JDBC", "MySQL"],
				["AsyncShim", "JRedis", "Redis"],
				["HTTP", ["S3", "AwesomeService"]],
				["Memcache", "Memcached"],
				["java-apns", "APNS"]
			]);
		},
		7: function() {
			Finagle.slideApp('slide-a').setServices([
				["Failure", "AsyncShim", "JDBC", "MySQL"],
				["Failure", "AsyncShim", "JRedis", "Redis"],
				["Failure", "HTTP", ["S3", "AwesomeService"]],
				["Failure", "Memcache", "Memcached"],
				["Failure", "java-apns", "APNS"]
			]);
		},
		8: function() {
			Finagle.slideApp('slide-a').setServices([
				["Failure", "Tracing", "AsyncShim", "JDBC", "MySQL"],
				["Failure", "Tracing", "AsyncShim", "JRedis", "Redis"],
				["Failure", "Tracing", "HTTP", ["S3", "AwesomeService"]],
				["Failure", "Tracing", "Memcache", "Memcached"],
				["Failure", "Tracing", "java-apns", "APNS"]
			]);
		},
		9: function() {
			Finagle.slideApp('slide-a').setServices([
				["Failure", "Tracing", "AsyncShim", "JDBC", "MySQL"],
				["Failure", "Tracing", "Discovery", "AsyncShim", "JRedis", ["Redis", "Redis-1"]],
				["Failure", "Tracing", "HTTP", ["S3", "AwesomeService"]],
				["Failure", "Tracing", "Discovery", "Memcache", ["Memcached", "Memcached-2", "Memcached-3"]],
				["Failure", "Tracing", "java-apns", "APNS"]
			]);
		}
	},
	'slide-b' : {
		'init': function() {
			
		},
		'-1': function() {
			Finagle.slideApp('slide-b').clear();
		},
		0: function() {
			Finagle.slideApp('slide-b').setServices([
				["Finagle", "MySQL"]
			]);
		},
		1: function() {
			Finagle.slideApp('slide-b').setServices([
				["Finagle", ["MySQL", "Redis"]]
			]);
		},
		2: function() {
			Finagle.slideApp('slide-b').setServices([
				["Finagle", ["MySQL", "Redis", "S3"]]
			]);
		},
		3: function() {
			Finagle.slideApp('slide-b').setServices([
				["Finagle", ["MySQL", "Redis", "S3", "AwesomeService"]]
			]);
		},
		4: function() {
			Finagle.slideApp('slide-b').setServices([
				["Finagle", ["MySQL", "Redis", "S3", "AwesomeService", "Memcached"]]
			]);
		},
		5: function() {
			Finagle.slideApp('slide-b').setServices([
				["Finagle", ["MySQL", "Redis", "S3", "AwesomeService", "Memcached", "APNS"]]
			]);
		},
		6: function() {
			Finagle.slideApp('slide-b').setServices([
				["Failure", "Finagle", ["MySQL", "Redis", "S3", "AwesomeService", "Memcached", "APNS"]]
			]);
		},
		7: function() {
			Finagle.slideApp('slide-b').setServices([
				["Failure", "Tracing", "Finagle", ["MySQL", "Redis", "S3", "AwesomeService", "Memcached", "APNS"]]
			]);
		},
		8: function() {
			Finagle.slideApp('slide-b').setServices([
				["Failure", "Tracing", "Discovery", "Finagle", ["MySQL", "Redis", "Redis-1", "S3", "AwesomeService", "Memcached", "Memcached-2", "Memcached-3", "APNS"]]
			]);
		}
	}
}