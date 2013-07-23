dojo.require("dojo.back");
dojo.back.init();


var state = {
    back: function() {
        invokeBack();
    },
    forward: function() {
        invokeForward();
    }
};
this.changeUrl = true;
dojo.back.setInitialState(state);

function doNav() {
    dojo.back.addToHistory(state);
}