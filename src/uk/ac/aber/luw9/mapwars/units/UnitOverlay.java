package uk.ac.aber.luw9.mapwars.units;

import java.util.ArrayList;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import uk.ac.aber.luw9.mapwars.GameMap;
import uk.ac.aber.luw9.mapwars.R;
import uk.ac.aber.luw9.mapwars.controllers.UnitController;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Location;
import android.util.Log;
import android.view.MotionEvent;

public class UnitOverlay extends Overlay {
	private Bitmap overlay_user, overlay_user_selected, overlay_enemy;
    private Bitmap tOverlay, uOverlay, eOverlay, usOverlay;
	private UnitController unitController;
	private ArrayList<Unit> unitsSelected = new ArrayList<Unit>();
	private int TAP_RADIUS = 20;
	private int UNIT_RADIUS = 25;
	private boolean unitSelecting;
	private Point unitSelectionStart = new Point(), unitSelectionEnd = new Point();
	private Boolean selectBox = false;
	
	public UnitOverlay(UnitController controller, GameMap map) {
		super(map.getApplicationContext());
		this.unitController = controller;
		map.addOverlay(this);
		
		overlay_user = BitmapFactory.decodeResource(map.getApplicationContext().getResources(), R.drawable.tank_1);
		overlay_user_selected = BitmapFactory.decodeResource(map.getApplicationContext().getResources(), R.drawable.tank_1_selected);
		overlay_enemy = BitmapFactory.decodeResource(map.getApplicationContext().getResources(), R.drawable.tank_2);
	}
	
	public boolean onTap(float tapX, float tapY, MapView mapView) {
        GeoPoint tapPoint = (GeoPoint) mapView.getProjection().fromPixels(tapX, tapY);
        Location tapLocation = new Location("point B");
        tapLocation.setLatitude(tapPoint.getLatitudeE6() / 1E6);
        tapLocation.setLongitude(tapPoint.getLongitudeE6() / 1E6);
		
        //Track if a new unit has been selected this tap
        boolean newUnit = false;
       
		//look for unit
		ArrayList<Unit> units = unitController.getUnits(true);
		
	    for (Unit unit : units) {
	    	
	    	GeoPoint unitPoint = unit.getLocation();
 
	        Location unitLocation = new Location("point A");
	        unitLocation.setLatitude(unitPoint.getLatitudeE6() / 1E6);
	        unitLocation.setLongitude(unitPoint.getLongitudeE6() / 1E6);
	        
	        double distance = tapLocation.distanceTo(unitLocation);
	        
	        Log.i("UnitOverlayTap", String.valueOf(distance));
	        
	        if (distance < TAP_RADIUS) {
		    	//If the unit is already selected, unselect
		    	if (unit.isSelected()) {
			    	unit.unselect();
			    	unitsSelected.remove(unit);
		        } else {
		        	unit.select();
		        	unitsSelected.add(unit);
		        }
		    	
		    	newUnit = true;
	        }
		}

	    //if no new units have been selected then user must have clicked an empty area thus move units
	    if (!newUnit) {
	    	Log.i("UnitOverlayMove", "moving");
			//move selected units to location
			for (Unit unit : unitsSelected) {
				unit.unselect();
				unitController.moveUnit(unit.getId(), tapPoint, true);
			}
			unitsSelected.clear();
	    }

	    //force redraw
	    mapView.invalidate();
		return false;
	}
	
	@Override
	public boolean onSingleTapConfirmed(MotionEvent event, MapView mapView) {
		onTap(event.getX(0), event.getY(0), mapView);
		return false;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event, MapView mapView) {
		if (selectBox) {
			Log.i("UnitOverlayTouch",  event.toString());
			
			int tmpX = Math.round(event.getX());
			int tmpY = Math.round(event.getY());
			
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				mapView.getProjection().fromMapPixels(tmpX, tmpY, unitSelectionStart);
			} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
				mapView.getProjection().fromMapPixels(tmpX, tmpY, unitSelectionEnd);
				unitSelecting = true;
				mapView.invalidate();
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				unitSelecting = false;
				
				
				//look for units within this range
				ArrayList<Unit> units = unitController.getUnits(true);
				
			    for (Unit unit : units) {
			    	
			    	GeoPoint unitPoint = unit.getLocation();
	
			    	Log.i("UnitOverlaySelect", unitPoint.toString());
			    	Log.i("UnitOverlaySelect", unitSelectionStart.toString());
			    	Log.i("UnitOverlaySelect", unitSelectionEnd.toString());
			    	Log.i("UnitOverlaySelect", "----");
			    	
			        if (unitPoint.getLongitudeE6() > unitSelectionStart.y
			        		&& unitPoint.getLongitudeE6() < unitSelectionEnd.y
			        		&& unitPoint.getLatitudeE6() > unitSelectionStart.x
			        		&& unitPoint.getLatitudeE6() < unitSelectionStart.x) {
			        	
						    	if (unit.isSelected()) {
							    	unit.unselect();
							    	unitsSelected.remove(unit);
						        } else {
						        	unit.select();
						        	unitsSelected.add(unit);
						        }
			        }
				}
				
				mapView.invalidate();
			}
			
			return true;
		} else {
			return false;
		}
	}
	
	@Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
       // super.draw(canvas, mapView, shadow);
		
		//Shadows are not used so skip this draw
		if (shadow)
			return;
		
        int radius = (int) mapView.getProjection().metersToEquatorPixels(UNIT_RADIUS);
        
        Point point = new Point();
        
		usOverlay = Bitmap.createScaledBitmap(overlay_user_selected, radius, radius, false);
		uOverlay = Bitmap.createScaledBitmap(overlay_user, radius, radius, false);
		eOverlay = Bitmap.createScaledBitmap(overlay_enemy, radius, radius, false);

        ArrayList<Unit> units = unitController.getUnits();
	    for (Unit unit : units) {
	    	mapView.getProjection().toPixels(unit.getLocation(), point);
	    	if (unit.getType() == UnitType.USER) {
	        	if (unit.isSelected()) {
	        		tOverlay = usOverlay;
	        	} else {
	        		tOverlay = uOverlay;
	        	}
	    	} else {
	    		tOverlay = eOverlay;
	    	}
	        Log.i("UnitOverlay", "Drawing [" + point.x + "," + point.y + "]");
	        canvas.drawBitmap(tOverlay, point.x, point.y, null);
        }

	    if (!units.isEmpty()) {
		    Unit unit = units.get(0);
		    
		    mapView.getProjection().toPixels(unit.getLocation(), point);
	    }
	    
		if (unitSelecting) {
			Paint   mPaint = new Paint();
			mPaint.setDither(true);
			mPaint.setColor(Color.RED);
			mPaint.setStyle(Paint.Style.STROKE);
			//Create dashed stroke
			mPaint.setPathEffect(new DashPathEffect(new float[] {10,10}, 0));
			mPaint.setStrokeWidth(2);
			
			canvas.drawRect(unitSelectionStart.x, unitSelectionStart.y, unitSelectionEnd.x, unitSelectionEnd.y, mPaint);
		}
    }
}
