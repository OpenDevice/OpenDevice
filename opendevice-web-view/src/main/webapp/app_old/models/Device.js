var app = app || {};


// Like OpenDevice JAVA-API
app.DeviceType = {
	ANALOG:1, 
	DIGITAL:2,
	SENSOR:3
};

// Like OpenDevice JAVA-API
app.DeviceCategory = {
	LAMP:1, 
	FAN:2,
	GENERIC:3,
	POWER_SOURCE : 4,
	GENERIC_SENSOR: 50,
	IR_SENSOR: 51
};


app.Device = Backbone.Model.extend({
/** @lends app.Device.prototype */
  urlRoot : 'device',
  noIoBind: false,
  socket:window.socket,
  defaults: {
  	id:null,
  	name: '',
  	value: app.Constants.VALUE_LOW,
  	type : app.DeviceType.DIGITAL,
  	category : app.DeviceCategory.POWER_SOURCE
  },

  validate: function(attribs){
    // if(attribs.title === undefined){
        // return "Remember to set a title for your todo.";
    // }
  },

 /**
   * @class app.Device - Device Class
   * @constructs
   *
   * Add docs...
   */
  initialize: function(){
  	
    this.on("invalid", function(model, error){
        console.log(error);
    });
    
    this.on("change", function(event, error){
//    	 this.save(event.changed);
    });
    
    _.bindAll(this, 'serverChange', 'serverDelete',  'modelCleanup');
    
    /*!
     * if we are creating a new model to push to the server we don't want
     * to iobind as we only bind new models from the server. This is because
     * the server assigns the id.
     */
    if (!this.noIoBind) {
      this.ioBind('update', this.serverChange, this);
      this.ioBind('delete', this.serverDelete, this);
      this.ioBind('setvalue', this.serverSetValue, this);
    }
  },
  
  toogleValue: function() {
  	this.set('value', ( this.get('value') == 1 ? 0 : 1 ));
  	this.save();
  },
  
  serverChange: function (data) {
    // Useful to prevent loops when dealing with client-side updates (ie: forms).
    data.fromServer = true;
    this.set(data);
  }, 
  serverDelete: function (data) {
    if (this.collection) {
      this.collection.remove(this);
    } else {
      this.trigger('remove', this);
    }
    this.modelCleanup();
  },
  serverSetValue: function (data) {
	  //Useful to prevent loops when dealing with client-side updates (ie: forms).
	  data.fromServer = true;
	  this.set(data);
  }, 
	  
  modelCleanup: function () {
    this.ioUnbindAll();
    return this;
  }
  
});


// --------------------------------
// DeviceCollection
// --------------------------------

app.DeviceCollection = Backbone.Collection.extend({
  model: app.Device,
  // localStorage: new Backbone.LocalStorage('OpenDevice-devices'),
  url: 'devices',
  socket:window.socket,
    
  initialize: function () {
    _.bindAll(this, 'serverCreate', 'collectionCleanup');
    this.ioBind('create', this.serverCreate, this);
  },
  serverCreate: function (data) {
    // make sure no duplicates, just in case
    
    if(data.id == 0) data.id = undefined; 
    
    var exists = this.get(data.id);
    if (!exists) {
      this.add(data);
    } else {
      data.fromServer = true;
      exists.set(data);
    }
  },
  collectionCleanup: function (callback) {
    this.ioUnbindAll();
    this.each(function (model) {
      model.modelCleanup();
    });
    return this;
  }  
});
