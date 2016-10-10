/* Simple JavaScript Inheritance, Inspired by base2 and Prototype
 * By John Resig http://ejohn.org/
 * Changes by Rivardo JL Rufino (allow private functions and vars)
 * https://github.com/ricardojlrufino/JSClass
 * MIT Licensed.
 */
//
(function () {
    var initializing = false;
    var fnTest = /xyz/.test(function () {}) ? /\b_super\b/ : /.*/;

    // The base Class implementation (does nothing)
    this.Class = function () {
    };

    // Create a new Class that inherits from this class
    Class.extend = function (prop) {

        var _super = this.prototype;

        // Instantiate a base class (but only create the instance,
        // don’t run the init constructor)
        initializing = true;
        var prototype = new this();
        initializing = false;

        // make dump object to clone "prototype"
        if(typeof prop == "function") prop = new prop();

        // Allow call supper inside functions
        var _superCall = (function (name, fn) {
            return function () {
                var tmp = this._super;

                // Add a new ._super() method that is the same method
                // but on the super-class
                this._super = _super[name];

                // The method only need to be bound temporarily, so we
                // remove it when we’re done executing
                var ret = fn.apply(this, arguments);
                this._super = tmp;

                return ret;
            };
        });

        // Copy the properties over onto the new prototype
        for (var name in prop) {
            // Check if we’re overwriting an existing function
            if(typeof prop[name] == "function" &&
                typeof _super[name] == "function" &&
                fnTest.test(prop[name])){
                prototype[name] = _superCall(name, prop[name]);
            }else{
                prototype[name] = prop[name]
            }
        }

        // The dummy class constructor
        function Class() {
            // All construction is actually done in the init method
            if (!initializing && this.init)
                this.init.apply(this, arguments);
        }

        // Populate our constructed prototype object
        Class.prototype = prototype;

        // Enforce the constructor to be what we expect
        Class.prototype.constructor = Class;

        // And make this class extendable
        Class.extend = arguments.callee;

        return Class;
    };
})();