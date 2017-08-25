/**
 When the user scrolls down the page, the side clock updates to the date
 of any date blocks it passes.

 The date blocks are hidden on the page.
 When the page loads, this script scans their "scroll positions" on the page
 and stores this in the dateMarks array.
 On scroll, it uses the array to check which dateMark is nearest.
 If the nearest dateMark has changed, then it updates the date.

 The actual animation of the values is done by another js library: odometer.
 */

var monthNames = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
var dateMarks = [];

var MAX_STROKE = 300; // The stroke that makes the circle complete.
var CLOCK_Y = 100; // The vertical position of the side clock on the page.

var lastKnownScrollPosition = 0;
var dateMarkID = 0;

window.addEventListener('load', function() {
    initDateMarks();
    initClocks();

    startScrollHandling();
});

/**
 Find all date blocks on the page and store their positions in dateMarks.
 */
function initDateMarks() {
    $('.date-block').each(function() {
        var value = $(this).text();
        var theDate = new Date(value);
        var top = $(this).offset().top;

        var dateMark = {};
        dateMark.date = theDate;
        dateMark.y = top;
        dateMarks.push(dateMark);
    });
}

/**
 * Initializes both clocks (top, sidebar) with first date mark.
 */
function initClocks() {
    if (dateMarks.length > 0) {
        setClockDateTime($('.story-header'), dateMarks[0].date);
        setClockDateTime($('.clock-side'), dateMarks[0].date);
    }
}

/**
 Note: the actual digit animation is handled by js library: odometer.
 */
function setClockDateTime(parentElement, dateMark) {
    /*
     Adding 100 is a Trick to pad zeros https://github.com/HubSpot/odometer/issues/106
     */
    var day = dateMark.getDate() + 100;
    parentElement.find('.clock-day').html(day);
    var hour = dateMark.getHours() + 100;
    parentElement.find('.clock-hour').html(hour);
    var minutes = dateMark.getMinutes() + 100;
    parentElement.find('.clock-minutes').html(minutes);
    var month = dateMark.getMonth();
    parentElement.find('.clock-month').html(monthNames[month]);
}

function startScrollHandling() {
    window.addEventListener('scroll', function() {
        lastKnownScrollPosition = window.scrollY;
        window.requestAnimationFrame(function() {
            updateClockData(lastKnownScrollPosition);
            updateClockCircleOutline(lastKnownScrollPosition)
        });
    });
}

/**
 Determine the closest date mark.
 If it has changed, set the new values on the side clock.
 */
function updateClockData(scrollPosition) {
    for (var i = 0; i < dateMarks.length; i++) {
        var dateMark = dateMarks[i];
        if (dateMark.y > (scrollPosition + CLOCK_Y)) {
            var newID = i - 1;

            if (dateMarkID != newID) {
                dateMarkID = newID;

                if (dateMarkID > -1) {
                    dateMark = dateMarks[dateMarkID]

                    setClockDateTime($('.clock-side'), dateMark.date);
                }
            }

            break;
        }
    }
}

/**
 Change the outline of the side clock circle.
 */
function updateClockCircleOutline(scrollPosition) {
    var maxScroll = $(document).height() - $(window).height();
    var scrollRatio = scrollPosition / maxScroll;
    var strokeOffset = MAX_STROKE - scrollRatio * MAX_STROKE;

    $('#clock-widget circle.clock-gauge').css("stroke-dashoffset", strokeOffset);
}
