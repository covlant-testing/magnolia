/**
 * Simple JavaScript model that provides two main functions:
 * a) getColor() returns a random color
 * b) getShoutOut() returns a random shout out.
 */
var StoriesImageUniverse = function() {
    this.colors = ["blue", "yellow", "green"];
    this.shoutOut = [
        i18n.translate("story.component.storiesImageUniverse.rndText1"),
        i18n.translate("story.component.storiesImageUniverse.rndText2"),
        i18n.translate("story.component.storiesImageUniverse.rndText3")
    ];
    this.getRandomNumber = function() {
        return Math.floor((Math.random() * 3));
    };
    this.getColor = function() {
        return this.colors[this.getRandomNumber()];
    };
    this.getShoutOut = function() {
        return this.shoutOut[this.getRandomNumber()];
    }
};

new StoriesImageUniverse();
