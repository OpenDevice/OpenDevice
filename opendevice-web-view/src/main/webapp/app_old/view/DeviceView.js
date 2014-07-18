var app = app || {};

app.DeviceView = Backbone.View.extend({
	
	  //localTemplate : _.template( $('#device-template').html() ),
	  // TODO: Mudar para template externo...
	  localTemplate: _.template('<a class="device-view" href="#" data-deviceid="1">'+
	  '<span class="device-view-icon mws-ic">&nbsp;</span>'+
      '<span class="device-view-content">'+
        	'<span class="device-view-title"><%- name %> #<%- id %></span>'+
            '<span class="device-view-value <%= (value == 1 ? \'on\' : \'off\') %>"><%= (value == 1 ? \'ON\' : \'OFF\') %></span>'+
        '</span>'+
     '</a>'),
     
     deviceIcons : function(){
     	var icons = {};
     	icons[app.DeviceCategory.LAMP] = ["ic-lightbulb-off","ic-lightbulb"];
     	icons[app.DeviceCategory.GENERIC] = ["ic-battery-low", "ic-battery-full"];
     	icons[app.DeviceCategory.POWER_SOURCE] = ["ic-battery-low", "ic-battery-full"];
     	return icons;
     },
     
     initialize: function() {
     	this.listenTo(this.model, 'change', this.render);
     },
 	
	  events: {
        click: function(e) {
          this.model.toogleValue();
        }
     },
     
	 render: function() {
		this.$el.html( this.localTemplate( this.model.toJSON() ) );
		
		this.$icon = this.$("span.device-view-icon");
		
		this.$icon.addClass(this.getIcon());
		
		// this.input = this.$('.edit');
	 	return this;
	 },
	 
	 getIcon : function(){
	 	var value = this.model.get('value');
	 	var category = this.model.get('category');
	 	
	 	var icons = this.deviceIcons();
	 	var iconsCat = icons[category];
	 	
	 	if(!category) category = app.DeviceCategory.GENERIC;
	 	
	 	var iconClass = (value == app.Constants.VALUE_HIGH  ? iconsCat[1] : iconsCat[0]);
	 	
	 	return iconClass;
	 }


});