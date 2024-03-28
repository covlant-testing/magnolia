// jquery.object-fit-cover-simple.js
// ====================
//
// A simple jquery plugin to do what the css 'object-fit' property does with value 'cover'.
// The image of the targeted item covers the entire area of the element.
// Useful in cases when a background-image cannot be used, such as when one wants to load images responsively with srcset.
// Only on elements of jquery selection.
// Parent of element to be used for sizing must have css display:block.
// Does nothing if browser supports object-fit css property, unless called with true
// in which case the fitting is forced.
//
// usage:
// ------
// ```
// $('.selector').objectFitCover();
// ```
// Elements must specify their aspect ratio as a decimal via attribute data-ratio.
// ex. <img src="..." data-ratio="1.333" />
//
// Based on work by by Simon Schmid https://github.com/schmidsi
// Re-worked by Steve Workman https://github.com/steveworkman
// Object-fit property in CSS3 Images http://www.w3.org/TR/2012/CR-css3-images-20120417/#object-fit


(function ($, window, document) {

    var parent;
    var $bg;
    var aspectRatio;

    var toResize = [];

    var browserSupportsObjectFit = function () {
        return ("objectFit" in document.body.style);
    };

    function findParent(jqObject) {
        var p = jqObject.parent();
        var displayType = p.css('display');
        if (displayType == 'block' || displayType == '-webkit-box' && p.width() > 0) {
            return p;
        } else {
            return findParent(p);
        }
    }

    var doObjectFit = function (forceFit) {
        if (!forceFit) {
            if (browserSupportsObjectFit()) {
                return;
            }
        }


        $(window).resize(resizeBg).trigger("resize");

        return this.each(function () {
            prepareResize(this);
        });


    }

    var prepareResize = function (elemDom) {
        var elem = $(elemDom);
        aspectRatio = Number(elem.attr("data-ratio"));
        parent = findParent(elem);
        var item = {elem: elem, ratio: aspectRatio, parent: parent};
        toResize.push(item);
        resizeItem(item);
    };

    function resizeBg() {
        for (var i = 0, len = toResize.length; i < len; i++) {
            var item = toResize[i];
            resizeItem(item);
        }
    }

    function resizeItem(item) {
        var pwidth = item.parent.width();
        var pheight = item.parent.height();

        if ((pwidth / pheight) < item.ratio) {
            // cover
            item.elem.css('width', 'auto');
            item.elem.css('height', '100%');
            // center
            var myLeft = ( item.ratio * pheight - pwidth ) / -2;
            item.elem.css('left', myLeft);
            item.elem.css('top', '0');

        } else {
            // cover
            item.elem.css('width', '100%');
            item.elem.css('height', 'auto');
            // center
            var myTop = ( (1 / item.ratio) * pwidth - pheight ) / -2;
            item.elem.css('left', '0');
            item.elem.css('top', myTop);

        }
    }

    $.fn.objectFitCoverSimple = doObjectFit;

})(jQuery, window, document);