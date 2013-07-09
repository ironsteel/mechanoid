package com.robotoworks.mechanoid.db.generator

import com.robotoworks.mechanoid.db.sqliteModel.ActionStatement
import com.robotoworks.mechanoid.db.sqliteModel.ColumnDef
import com.robotoworks.mechanoid.db.sqliteModel.ColumnSource
import com.robotoworks.mechanoid.db.sqliteModel.CreateTableStatement
import com.robotoworks.mechanoid.db.sqliteModel.Model

import static extension com.robotoworks.mechanoid.text.Strings.*
import static extension com.robotoworks.mechanoid.db.util.ModelUtil.*
import com.robotoworks.mechanoid.db.sqliteModel.DDLStatement
import com.robotoworks.mechanoid.db.sqliteModel.CreateViewStatement
import com.robotoworks.mechanoid.db.util.ModelUtil
import com.robotoworks.mechanoid.db.sqliteModel.ResultColumn
import com.robotoworks.mechanoid.db.generator.SqliteDatabaseSnapshot
import com.robotoworks.mechanoid.db.sqliteModel.ContentUri
import com.robotoworks.mechanoid.db.sqliteModel.ContentUriParamSegment
import java.util.ArrayList
import com.robotoworks.mechanoid.db.sqliteModel.TableDefinition
import com.robotoworks.mechanoid.db.sqliteModel.CreateTableStatement

class ContentProviderContractGenerator {
		def CharSequence generate(Model model, SqliteDatabaseSnapshot snapshot) { 
			'''		
			/*
			 * Generated by Robotoworks Mechanoid
			 */
			package �model.packageName�;
			
			import android.net.Uri;
			import android.provider.BaseColumns;
			import com.robotoworks.mechanoid.Mechanoid;
			import com.robotoworks.mechanoid.db.AbstractValuesBuilder;
			import java.lang.reflect.Field;			
			import java.util.Collections;
			import java.util.HashSet;
			import java.util.HashMap;
			import java.util.Set;
			import java.util.Map;
			
			public class �model.database.name.pascalize�Contract  {
			    public static final String CONTENT_AUTHORITY = initAuthority();

				private static String initAuthority() {
					String authority = "�model.packageName�.�model.database.name.toLowerCase�";
			
					try {
			    		
			    		ClassLoader loader = �model.database.name.pascalize�Contract.class.getClassLoader();
			    		
						Class<?> clz = loader.loadClass("�model.packageName�.�model.database.name.pascalize�ContentProviderAuthority");
						Field declaredField = clz.getDeclaredField("CONTENT_AUTHORITY");
						
						authority = declaredField.get(null).toString();
					} catch (ClassNotFoundException e) {} 
			    	catch (NoSuchFieldException e) {} 
			    	catch (IllegalArgumentException e) {
					} catch (IllegalAccessException e) {
					}
					
					return authority;
				}
				
			    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
			
				�FOR tbl : snapshot.tables�
				interface �tbl.name.pascalize�Columns {
					�FOR col : tbl.columnDefs.filter([!name.equals("_id")])�
					String �col.name.underscore.toUpperCase� = "�col.name�";
					�ENDFOR�
				}
				
				�ENDFOR�

				�FOR vw :  snapshot.views�
				interface �vw.name.pascalize�Columns {
					�FOR col : vw.getViewResultColumns.filter([!name.equals("_id")])�
					�generateInterfaceMemberForResultColumn(col)�
					�ENDFOR�
				}
				
				�ENDFOR�
						
				�FOR tbl : snapshot.tables�
				�generateContractItem(model, snapshot, tbl)�
				�ENDFOR�
			
				�FOR vw : snapshot.views�
				�generateContractItem(model, snapshot, vw)�
				�ENDFOR�
				
				static Map<Uri, Set<Uri>> REFERENCING_VIEWS;
				
				static {
					Map<Uri, Set<Uri>> map = new HashMap<Uri, Set<Uri>>();
					
					�FOR tbl : snapshot.tables�
					map.put(�tbl.name.pascalize�.CONTENT_URI, �tbl.name.pascalize�.VIEW_URIS);
					�ENDFOR�
					�FOR vw : snapshot.views�
					map.put(�vw.name.pascalize�.CONTENT_URI, �vw.name.pascalize�.VIEW_URIS);
					�ENDFOR�
					
					REFERENCING_VIEWS = Collections.unmodifiableMap(map);
					
				}
				
				�generateContractItemsForActions(model, snapshot)�
				private �model.database.name.pascalize�Contract(){}
				
				/**
				 * <p>Delete all rows from all tables</p>
				 */						
				public static void deleteAll() {
					�FOR tbl : snapshot.tables�
					�tbl.name.pascalize�.delete();
					�ENDFOR�
				}
			}
			'''
	}
	
	def generateContractItemsForActions(Model model, SqliteDatabaseSnapshot snapshot) '''
		�IF model.database.config != null�
		�FOR action : model.database.config.statements
			.filter(typeof(ActionStatement))
			.filter([!snapshot.containsDefinition(it.uri.type)])
		�
		public static class �action.uri.type.pascalize� {
			�createActionUriBuilderMethod(action)�
			public static final String CONTENT_TYPE =
			        "vnd.android.cursor.dir/vnd.�model.database.name.toLowerCase�.�action.uri.type�";
		}

		�ENDFOR�
		�ENDIF�	
	'''
	
	def createActionUriBuilderMethod(ActionStatement action) '''
		public static Uri build�action.name.pascalize�Uri(�action.uri.toMethodArgs�) {
			return BASE_CONTENT_URI
				.buildUpon()
				.appendPath("�action.uri.type�")
				�FOR seg : action.uri.segments�
				�IF seg instanceof ContentUriParamSegment�
				�IF (seg as ContentUriParamSegment).num�
				.appendPath(String.valueOf(�seg.name.camelize�))
				�ELSE�
				.appendPath(�seg.name.camelize�)
				�ENDIF�
				�ELSE�
				.appendPath("�seg.name�")
				�ENDIF�
				�ENDFOR�
				.build();
		}
		
	'''
	
	/*
	 * Find all actions associated to the given definition,
	 * actions are associated to the definition via the first
	 * part of an action uri, for instance /recipes/a/b/c is
	 * associated to recipes
	 */
	def Iterable<ActionStatement> findActionsForDefinition(Model model, String defName) {
		if(model.database.config == null) {
			return new ArrayList<ActionStatement>();
		}
		
		return model.database.config.statements
			.filter(typeof(ActionStatement))
			.filter([action|action.uri.type.equals(defName)])
	}
	
	
	def toMethodArgs(ContentUri uri) {
		uri.segments
			.filter(typeof(ContentUriParamSegment))
			.join(", ", [seg|(
				if(seg.num) {
					return "long " + seg.name.camelize
				} else {
					return "String " + seg.name.camelize
				})])
	}
	
	def hasMethodArgs(ContentUri uri) {
		uri.segments
			.filter(typeof(ContentUriParamSegment)).size > 0
	}

	
	def generateContractItem(Model model, SqliteDatabaseSnapshot snapshot, TableDefinition stmt) '''
		/**
		 * <p>Column definitions and helper methods to work with the �stmt.name.pascalize�.</p>
		 */
		public static class �stmt.name.pascalize� implements �stmt.name.pascalize�Columns�IF stmt.hasAndroidPrimaryKey�, BaseColumns�ENDIF� {
		    public static final Uri CONTENT_URI = 
					BASE_CONTENT_URI.buildUpon().appendPath("�stmt.name.toLowerCase�").build();
		
			/**
			 * <p>The content type for a cursor that contains many �stmt.name.pascalize� rows.</p>
			 */
		    public static final String CONTENT_TYPE =
		            "vnd.android.cursor.dir/vnd.�model.database.name.toLowerCase�.�stmt.name�";

			�IF stmt.hasAndroidPrimaryKey�
			/**
			 * <p>The content type for a cursor that contains a single �stmt.name.pascalize� row.</p>
			 */
			public static final String ITEM_CONTENT_TYPE =
				"vnd.android.cursor.item/vnd.�model.database.name.toLowerCase�.�stmt.name�";
		    �ENDIF�
		
			/**
			 * <p>Builds a Uri with appended id for a row in �stmt.name.pascalize�, 
			 * eg:- content://�model.packageName�.�model.database.name.toLowerCase�/�stmt.name.toLowerCase�/123.</p>
			 */
		    public static Uri buildUriWithId(long id) {
		        return CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
		    }
		    �var actions = model.findActionsForDefinition(stmt.name)�
			�IF actions != null�
			�FOR action : actions�
			�action.createActionUriBuilderMethod�
			
			�ENDFOR�
			�ENDIF�
			public static int delete() {
				return Mechanoid.getContentResolver().delete(�stmt.name.pascalize�.CONTENT_URI, null, null);
			}
			
			public static int delete(String where, String[] selectionArgs) {
				return Mechanoid.getContentResolver().delete(�stmt.name.pascalize�.CONTENT_URI, where, selectionArgs);
			}
			
			/**
			 * <p>Create a new Builder for �stmt.name.pascalize�</p>
			 */
			public static Builder newBuilder() {
				return new Builder();
			}
			
			/**
			 * <p>Build and execute insert or update statements for �stmt.name.pascalize�.</p>
			 *
			 * <p>Use {@link �stmt.name.pascalize�#newBuilder()} to create new builder</p>
			 */
			public static class Builder extends AbstractValuesBuilder {
				private Builder() {
					super(Mechanoid.getApplicationContext(), �stmt.name.pascalize�.CONTENT_URI);
				}
				
				�generateBuilderSetters(stmt)�
			}
			
			static final Set<Uri> VIEW_URIS;
			
			static {
				HashSet<Uri> viewUris =  new HashSet<Uri>();

				�FOR ref : snapshot.getAllViewsReferencingTable(stmt)�
				viewUris.add(�ref.name.pascalize�.CONTENT_URI);
				�ENDFOR�
				
				VIEW_URIS = Collections.unmodifiableSet(viewUris);
			}
		}
	'''
	
	def dispatch generateBuilderSetters(CreateTableStatement stmt) '''
		�FOR item : stmt.columnDefs.filter([!name.equals("_id")])�
		�var col = item as ColumnDef�
		public Builder set�col.name.pascalize�(�col.type.toJavaTypeName� value) {
			mValues.put(�stmt.name.pascalize�.�col.name.underscore.toUpperCase�, value);
			return this;
		}
		�ENDFOR�
	'''
	
	def dispatch generateBuilderSetters(CreateViewStatement stmt) '''
		�var cols = stmt.viewResultColumns�
		�FOR item : cols.filter([!name.equals("_id")])�
		�var col = item as ResultColumn�
		�var type = col.inferredColumnType�
		public Builder set�col.name.pascalize�(�type.toJavaTypeName� value) {
			mValues.put(�stmt.name.pascalize�.�col.name.underscore.toUpperCase�, value);
			return this;
		}
		�ENDFOR�
	'''

	
	def dispatch getName(CreateTableStatement stmt) {
		stmt.name
	}
	
	def dispatch getName(CreateViewStatement stmt) {
		stmt.name
	}
	
	def dispatch hasAndroidPrimaryKey(CreateTableStatement stmt) {
		ModelUtil::hasAndroidPrimaryKey(stmt)
	}
	
	def dispatch hasAndroidPrimaryKey(CreateViewStatement stmt) {
		ModelUtil::hasAndroidPrimaryKey(stmt)
	}
	
	
	def createMethodArgsFromColumns(CreateTableStatement tbl) {
		'''�FOR item : tbl.columnDefs.filter([!name.equals("_id")]) SEPARATOR ", "��var col = item as ColumnDef��col.type.toJavaTypeName()� �col.name.camelize��ENDFOR�'''
	}
	
	def generateInterfaceMemberForResultColumn(ColumnSource expr) { 
		'''
		�IF expr.name != null && !expr.name.equals("") && !expr.name.equals("_id")�
		String �expr.name.underscore.toUpperCase� = "�expr.name�";
		�ENDIF�
		'''		
	}
	
}