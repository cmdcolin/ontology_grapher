var cytoscape = require('cytoscape');
var cydagre = require('cytoscape-dagre');
var cyqtip = require('cytoscape-qtip');
var panzoom = require('cytoscape-panzoom');
var dagre = require('dagre');
var _ = require('underscore');
var utils = require('./js/util');
var chroma = require('chroma-js');

var nodesCy = {};
var edgesCy = {};
var nodeScores = {};
var relationships = [];
var ontologyTerms = {};
var ontologyIds = {};
var cygraph;


var scales = (elt) => {
    var colorPalette = chroma.scale('Set1').colors(relationships.length);
    return elt === 'parents' ? '#333' : colorPalette[relationships.indexOf(elt) - 1];
};


function processParents(cy, graph, term, depth) {
    var node = graph[term];
    if (!node) {
        return;
    }
    if (!nodesCy[term]) {
        nodesCy[term] = {
            data: {
                id: term,
                label: utils.explode(node.description, 22),
                score: -Math.log(nodeScores[term]) * 150,
                pval: nodeScores[term],
            },
        };
    }
    relationships.forEach((elt) => {
        var list = node[elt];
        if (list) {
            list.forEach((tangentialTerm) => {
                var tangentialNode = graph[tangentialTerm];
                if (!nodesCy[tangentialTerm]) {
                    nodesCy[tangentialTerm] = {
                        data: {
                            id: tangentialTerm,
                            label: utils.explode((tangentialNode || {}).description || tangentialTerm, 22),
                        },
                    };
                }
            });
        }
    });
    if (node.parents) {
        for (var i = 0; i < node.parents.length; i++) {
            if (depth < depthLimit) {
                processParents(cy, graph, node.parents[i], depth + 1);
            }
        }
    }
}


function processParentsEdges(cy, graph, term, depth) {
    var node = graph[term];
    if (!node) {
        return;
    }

    relationships.forEach((elt) => {
        if (node[elt]) {
            for (var i = 0; i < node[elt].length; i++) {
                var edgeName = `${term},${node[elt][i]}-${elt}`;
                if (!edgesCy[edgeName]) {
                    var target = node[elt][i];
                    var source = term;

                    edgesCy[edgeName] = {
                        data: {
                            label: elt,
                            id: edgeName,
                            target: target,
                            source: source,
                        },
                    };
                    if (depth < depthLimit && elt === 'parents') {
                        processParentsEdges(cy, graph, node[elt][i], depth + 1);
                    }
                }
            }
        }
    });
}

function setupGraph(graph, term) {
    var stylesheetCy = cytoscape.stylesheet()
        .selector('node')
        .style({
            content: 'data(label)',
            'text-valign': 'center',
            'background-color': elt => (elt.data('score') ? `hsl(${elt.data('score') / -Math.log(_.min(_.values(nodeScores)))}, 50%, 50%)` : '#fff'),
            'border-color': '#333',
            'border-width': 5,
            shape: 'rectangle',
            'text-max-width': '1000px',
            'text-wrap': 'wrap',
            width: 'label',
            'padding-left': '9px',
            'padding-bottom': '9px',
            'padding-right': '9px',
            'padding-top': '9px',
            height: 'label',
        })
        .selector('edge')
        .css({
            'target-arrow-shape': 'triangle',
            'curve-style': 'bezier',
            'target-arrow-color': elt => scales(elt.data('label')),
            'line-color': elt => scales(elt.data('label')),
            width: 5,
        });

    nodesCy = {};
    edgesCy = {};
    if (cygraph) {
        cygraph.destroy();
    }

    if (_.isArray(term)) {
        _.each(term, (m) => {
            processParents(cygraph, graph, m, 0);
            processParentsEdges(cygraph, graph, m, 0);
        });
    } else {
        processParents(cygraph, graph, term, 0);
        processParentsEdges(cygraph, graph, term, 0);
    }
    cygraph = cytoscape({
        container: $('#cy'),
        style: stylesheetCy,
        elements: {
            nodes: _.values(nodesCy),
            edges: _.values(edgesCy),
        },
    });
    var defaults = {
        zoomFactor: 0.05,
        zoomDelay: 45,
        minZoom: 0.1,
        maxZoom: 10,
        fitPadding: 50,
        panSpeed: 10,
        panDistance: 10,
        panDragAreaSize: 75,
        panMinPercentSpeed: 0.25,
        panInactiveArea: 8,
        panIndicatorMinOpacity: 0.5,
        zoomOnly: false,
        fitSelector: undefined,
        animateOnFit: () => false,
        fitAnimationDuration: 1000,
        sliderHandleIcon: 'fa fa-minus',
        zoomInIcon: 'fa fa-plus',
        zoomOutIcon: 'fa fa-minus',
        resetIcon: 'fa fa-expand',
    };

    cygraph.panzoom(defaults);

    cygraph.elements().qtip({
        content: function () { return `<b>${this.data('id')}</b><br />${this.data('label')}<br />${this.data('pval') ? `pval: ${this.data('pval')}` : ''}`; },
        position: {
            my: 'top center',
            at: 'bottom center',
        },
        style: {
            classes: 'qtip-bootstrap',
            'font-family': 'sans-serif',
            tip: {
                width: 16,
                height: 8,
            },
        },
    });

    // Manually crate and stop layout after timeout
    var layoutCy = cygraph.makeLayout({
        name: 'dagre',
        rankDir: 'BT',
        padding: 50,
        randomize: true,
        animate: true,
        repulsion: 1,
    });

    layoutCy.run();
}

function downloadAndSetupGraph(term) {
    $('#search').val(ontologyIds[term]);
    $('#loading').text('Loading...');

    

    $.ajax({ url: 'http://localhost:4567/ontology', dataType: 'json' }).done((res) => {
        setupGraph(res, term);
        $('#loading').text('');
    });
}

function setupEventHandlers() {
    // Event handlers
    $('#termform').submit(() => {
        var term = $('#term').val();
        window.history.replaceState({}, '', `?term=${term}`);
        downloadAndSetupGraph(term);
        return false;
    });



    $('#searchform').submit(() => {
        var search = $('#search').val();
        var term = ontologyTerms[search];
        $('#term').val(term);
        window.history.replaceState({}, '', `?term=${term}`);
        downloadAndSetupGraph(term);
        return false;
    });

    $('#multi').submit(() => {
        var nodes = [];
        var pvals = [];
        var goterms = $('#goterms').val().split('\n');
        goterms.forEach((line) => {
            var matches = line.split(/\s+/);
            if (matches.length === 2) {
                nodes.push(matches[0]);
                pvals.push(matches[1]);
                nodeScores[matches[0]] = parseFloat(matches[1]);
            }
        });
        window.history.replaceState({}, '', `?terms=${nodes.join(',')}&pvals=${pvals.join(',')}`);
        downloadAndSetupGraph(nodes, pvals);
        return false;
    });
}

$(() => {
    // Check query params
    var terms = utils.getParameterByName('terms');
    var pvals = utils.getParameterByName('pvals');
    var term = utils.getParameterByName('term');

    if (terms && pvals) {
        terms = terms.split(',');
        pvals = pvals.split(',');
        var str = '';
        if (terms.length === pvals.length) {
            for (var i = 0; i < terms.length; i++) {
                str += `${terms[i]}\t${pvals[i]}\n`;
                nodeScores[terms[i]] = parseFloat(pvals[i]);
            }
            $('#goterms').val(str);
        }
        downloadAndSetupGraph(terms, pvals);
    } else if (term) {
        $('#term').val(term);
        downloadAndSetupGraph(term);
    } else if ($('#term').val()) {
        downloadAndSetupGraph($('#term').val());
    }
    cydagre(cytoscape, dagre);
    cyqtip(cytoscape, $);
    cydagre(cytoscape, dagre);
    panzoom(cytoscape, $);


    setupEventHandlers();
});
