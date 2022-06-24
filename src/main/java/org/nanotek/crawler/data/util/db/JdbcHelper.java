package org.nanotek.crawler.data.util.db;

import static net.bytebuddy.matcher.ElementMatchers.named;

import java.beans.PropertyEditorManager;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.sql.DataSource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Type;
import org.nanotek.crawler.Base;
import org.nanotek.crawler.BaseEntity;
import org.nanotek.crawler.data.config.meta.IClass;
import org.nanotek.crawler.data.config.meta.IDataAttribute;
import org.nanotek.crawler.data.config.meta.MetaClass;
import org.nanotek.crawler.data.config.meta.MetaDataAttribute;
import org.nanotek.crawler.data.config.meta.MetaRelationClass;
import org.nanotek.crawler.data.util.buddy.BuddyBase;
import org.nanotek.crawler.data.util.db.support.MetaClassPostProcessor;
import org.nanotek.crawler.legacy.util.Holder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.ClassFileVersion;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.dynamic.loading.InjectionClassLoader;
import net.bytebuddy.implementation.FixedValue;
import schemacrawler.inclusionrule.RegularExpressionInclusionRule;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schemacrawler.LimitOptionsBuilder;
import schemacrawler.schemacrawler.LoadOptionsBuilder;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.schemacrawler.SchemaInfoLevelBuilder;
import schemacrawler.tools.utility.SchemaCrawlerUtility;

@Slf4j
public class JdbcHelper {
	
	public static final String PACKAGE =  "org.nanotek.entity.mb.";  ;


	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	DataSource dataSource;

	@Autowired
	PersistenceUnityClassesConfig entityClassConfig;
	
	List<MetaClassPostProcessor<MetaClass>> processors;

	public JdbcHelper() {
		postConstruct();
	}

	void postConstruct() {
		processors = new ArrayList<>();
	}

	

	public static String camelToSnake(String str) {
		return Optional
				.ofNullable(str)
				.map(str1 -> {
					String name = str1;
					Pattern pattern = Pattern.compile("^([A-Z]+),");
					Matcher matcher = pattern.matcher(name);
					while (matcher.find()) {
						System.out.println("matcher");
						int pos = matcher.start();
						String res = str1.substring(pos, pos+1);
						String str2 =  name.replaceFirst("[A-Z]", res);
						name=str2;
					}
					return name;
				}).orElse(str);
	}
	
	//TODO: Find the last version that fix this method. 
	//its somewhere on computer... find it.
	// Function to convert the string
	// from snake case to camel case
	public static String snakeToCamel(String str)
	{
		str = str.toLowerCase();
//		 Capitalize first letter of string
		str = str.substring(0, 1).toLowerCase()
				+ str.substring(1);

		// Run a loop till string
		// string contains underscore
		while (str.contains("_")) {

			// Replace the first occurrence
			// of letter that present after
			// the underscore, to capitalize
			// form of next letter of underscore
			str = str
					.replaceFirst(
							"_[a-z]",
							String.valueOf(
									Character.toUpperCase(
											str.charAt(
													str.indexOf("_") + 1))));
			Pattern pat = Pattern.compile("_[0-9]+");
			Matcher mat = pat.matcher(str);
			if (mat.find()) {
				String subsequence = mat.group();
			str = str
					.replaceFirst(
							"_[0-9]+",subsequence.substring(1));
			}
		}

//		// Return string
		return str;
	}


	public  Class<?>  generateBaseClass(MetaClass cm, InjectionClassLoader classLoader) throws Exception{
		//		    	 ResultSetMetaData meta = rs.getMetaData();
		Class<?> baseClass =  createBaseClass(cm , classLoader);
		postProcessBaseClass(baseClass);
		return baseClass;
	}

	
	
	private void postProcessBaseClass(Class<?> baseClass) {
	}

	public Class<?> createBaseClass(MetaClass cm, InjectionClassLoader classLoader) {
		String myClassName = cm.getClassName();
		String myClassName1 = prepareName(myClassName);
		log.info( "my class name {}:",  myClassName);
		Class<?> baseClass =  Optional.of(cm).filter(cm1 -> cm1.getMetaAttributes().stream().anyMatch(cm11 -> cm11.isId()))
				.map(cm11 ->{
					List<MetaDataAttribute> metaAttributes = cm11.getMetaAttributes();
					Builder bd = processClassMetaData (cm , classLoader);
					Holder<Builder> h = new Holder<>();
					h.put(bd);
					fixPrimaryKey(cm);
					metaAttributes
					.stream()
					.forEach(m -> {
						m.setMetaClass(cm11);
						checkClass(m);
						processMetaAttribute(m , h);
					});
					
					 Class<?> c = createBuddyClass(h , myClassName1 , classLoader);
					 return c;
					}).orElse(null);
		return baseClass;
	}

	
	private void fixPrimaryKey(MetaClass cm) {
		if (cm.getMetaAttributes().parallelStream().filter(a -> a.isId()).count()>1) {
			createPkClass(cm);
		}else if (cm.getMetaAttributes().parallelStream().filter(a -> a.isId()).count()==1){
			log.info("class name {} " , cm.getClassName());
		}else { 
			throw new RuntimeException("fuck fuck fuck");
		}
	}

	private void createPkClass(MetaClass cm) {
			log.info("class name {} " , cm.getClassName());
	}

	private void checkClass(IDataAttribute m) {
		if (PropertyEditorManager.findEditor(m.getClazz()) == null) {
			if (BigDecimal.class.equals(m.getClazz())) {
				m.setClazz(Double.class);
			}else if (java.sql.Date.class.equals(m.getClazz())){
				m.setClazz(java.util.Date.class);
			}else {
				m.setClazz(String.class);
			}
		}
	}

	private Class<?> createBuddyClass(Holder<Builder> h, String myClassName, InjectionClassLoader classLoader) {
		Builder theBuilder = h.get().orElseThrow();
		 Class<?> c = theBuilder.make().load(classLoader).getLoaded();
		 Entity theEntity = c.getAnnotation(Entity.class);
		 if (!isValidEntity(c)){ 
			 System.err.println("IS NOT VALID ENTITY PK ---------------" + theEntity.name());
			throw new RuntimeException();
		 }
		 if (isMultiplePk(c)){ 
			 System.err.println("IS NULL PK ---------------" + theEntity.name());
				throw new RuntimeException();
			 }
		 entityClassConfig.put(myClassName, c);
		 entityClassConfig.getTypeCache().insert(classLoader, myClassName , c);
		 System.err.println("Printing annotations for class " + theEntity.name());
		 if (entityClassConfig.getOrDefault(c.getName(), Base.class).equals(Base.class)) {
			 entityClassConfig.put(c.getSimpleName(), c);
		 }else {
			 Class<?> oldValue = entityClassConfig.get(c.getSimpleName());
			 entityClassConfig.replace(c.getSimpleName(), oldValue, c);
		 }
		return c;
	}
	
	private boolean isValidEntity(Class<?> c) {
		return   Arrays.asList(c .getDeclaredFields()).stream()
				.filter(f -> f.getAnnotation(Id.class) !=null).count()>0;
	}

	private boolean isMultiplePk(Class<?> c) {
		return   Arrays.asList(c .getDeclaredFields()).stream()
				.filter(f -> f.getAnnotation(Id.class) !=null).count()>1;
	}
	/*
	 * 
	 * 	@org.nanotek.crawler.data.config.meta.Id
	@Id
	@Column (name="id")
	@JsonProperty(value = "id")
	 */

	private void processMetaAttribute(MetaDataAttribute m , Holder<Builder> h) {
		try {
			String name = Optional.ofNullable( m.getColumnName() ).orElse(m.getColumnName());
			String fieldNAme = prepareAttributeNameName(m.getFieldName());
			name="\""+name+"\"";	
			log.info("field Name {}" , fieldNAme);
			AnnotationDescription columnAnnotation =  AnnotationDescription.Builder.ofType(Column.class)
					.define("name", name)
					.build();
			Class<?> ca = Class.class.cast( m.getClazz()  ).equals(Object.class) ? String.class : Class.class.cast(m.getClazz()) ;
			
			
			try {
				Builder bd1  = h.get().orElseThrow();
				if (m !=null && m.isId()) {
					bd1 = processIdProperty(bd1 , m , fieldNAme , ca , columnAnnotation ,  m.getMetaClass());
				}else {
					bd1=bd1.defineProperty(fieldNAme.trim(), ca).annotateField(checkAditionalAnnotatoins(ca , columnAnnotation));
				}
				h.put(bd1);
			}catch (Exception e) {
					log.info("error {}" , e);
					throw new RuntimeException(e);
				}
		} catch (Exception e1) {
			e1.printStackTrace();
			throw new RuntimeException(e1);
		}
		log.info("the holder value ");
	}

	private AnnotationDescription[] checkAditionalAnnotatoins(Class<?> ca, AnnotationDescription columnAnnotation) {
		AnnotationDescription[] descs ; 
		if (UUID.class.equals(ca)) {
			AnnotationDescription typed = AnnotationDescription.Builder.ofType(Type.class).define("type", "org.hibernate.type.PostgresUUIDType").build();
			descs = new 	AnnotationDescription[] {columnAnnotation,typed};
		}else {
			descs = new 	AnnotationDescription[] {columnAnnotation};
		}
		return descs;
	}

	@Autowired
	ObjectMapper mapper;
	
	private Builder processIdProperty(Builder bd1, MetaDataAttribute m , 
				String fieldNAme, Class<?> ca, AnnotationDescription columnAnnotation, MetaClass mc) {
		String fieldname = prepareAttributeNameName(m.getFieldName());
		System.err.println("PROCESSING ID FOR CLASS " + mc.getClassName().toUpperCase());
		System.err.println("-------------------- clsss " + mc.getClassName().toUpperCase() + "------------id " + fieldname);
		try {
			JsonNode ndoe =  mapper.convertValue(m, JsonNode.class);
			StringWriter writer = new StringWriter();
			mapper.writeValue(writer, ndoe);
			log.info(writer.getBuffer().toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.info("property ID :{}" , fieldname.trim());
		return bd1.defineProperty(fieldname.trim(), ca).annotateField(columnAnnotation).annotateField(processIdAnnotations());
	}

	private AnnotationDescription[] processIdAnnotations() {
		log.info("entering on process anottations");
		return new AnnotationDescription[] {
				AnnotationDescription.Builder.ofType(Id.class)
						.build(),
						AnnotationDescription.Builder.ofType(org.nanotek.crawler.data.config.meta.Id.class)
						.build(),
				AnnotationDescription.Builder.ofType(JsonProperty.class).define("value", "id").build()
		}; 
	}

	private Builder processClassMetaData(IClass cm11, ClassLoader classLoader) {
		processors.stream().forEach(p -> p.process(cm11));
		String tableName =  cm11.getTableName();
		String classNameCandidate = cm11.getClassName();
		String myClassName = prepareName(classNameCandidate);
		System.err.println("class name " + classNameCandidate);
		AnnotationDescription rootAnnotation =  AnnotationDescription.Builder.ofType(JsonRootName.class)
				.define("value", myClassName)
				.build();
		
		Class<?> idClass = getIdClass(cm11);
		
		BuddyBase bb = new BuddyBase();  
		TypeDefinition td = TypeDescription.Generic.Builder.parameterizedType(Base.class  , Base.class.asSubclass(BaseEntity.class) , idClass).build();
		Builder bd = new ByteBuddy(ClassFileVersion.JAVA_V8)
				.subclass(td)
//				.subclass(Base.class)
				.name(PACKAGE+myClassName)
				.annotateType(rootAnnotation)
//				.annotateType(infoAnnotation)
				.annotateType(new EntityImpl(myClassName))
				.annotateType(new TableImpl(tableName))
				.withHashCodeEquals()
				.withToString()
//				.defineProperty("metaClass", MetaClass.class)
//				.annotateField(AnnotationDescription.Builder.ofType(Transient.class).build())
//				.annotateField(AnnotationDescription.Builder.ofType(JsonIgnore.class).build())
				.method(named("getMetaClass")).intercept(FixedValue.value(MetaClass.class.cast(cm11)));
			return bd;
	}
	
	public Class<?> getIdClass(IClass metaClass){
		return metaClass.getMetaAttributes()
		.stream()
		.filter(att -> att.isId())
		.findAny().orElseThrow().getClazz();
		
	}


	private AnnotationDescription[] buildAnnotations(MetaDataAttribute m) {
		List<AnnotationDescription> list = new ArrayList<>();
		Optional.ofNullable(m)
										.filter(t ->t.isRequired())
										.filter(t -> t.getClazz().equals(String.class))
										.map(t1 ->  {return AnnotationDescription.Builder.ofType(NotEmpty.class).build();})
										.ifPresentOrElse( ad -> list.add(ad) , ()-> list.add(AnnotationDescription.Builder.ofType(NotNull.class).build()));
		
		Optional.ofNullable(m)
				.filter(t ->t.getLength() !=null && !t.getLength().isEmpty())
				.filter(t -> t.getClazz().equals(String.class))
				.map(t1 ->  {return AnnotationDescription.Builder.ofType(Size.class).define("max", Integer.valueOf(t1.getLength())) .build();})
				.ifPresent(ad -> list.add(ad));
		
		return list.toArray(new AnnotationDescription[list.size()]);
	}

	public static String prepareName(String classNameCandidate) {
		String prov = snakeToCamel(classNameCandidate.replaceAll(" ", ""));
		String first = prov.substring(0, 1).toUpperCase();
		return first.concat(prov.substring(1));
	}

	public static String prepareAttributeNameName(String classNameCandidate) {
		String prov = snakeToCamel(classNameCandidate.replaceAll(" ", ""));
		String first = prov.substring(0, 1).toLowerCase();
		return first.concat(prov.substring(1));
	}
	
	public  List<MetaClass> getClassMaps() throws Exception {

		SchemaInfoLevelBuilder vuilder = SchemaInfoLevelBuilder.builder()
							.setRetrieveAdditionalColumnAttributes(true)
							.setRetrieveAdditionalColumnMetadata(false)
							.setRetrieveColumnDataTypes(true)
							.setRetrieveForeignKeys(false)
							.setRetrieveIndexes(true)
							.setRetrieveIndexInformation(true)
							.setRetrieveTriggerInformation(false)
							.setRetrievePrimaryKeys(true)
							.setRetrieveTableColumns(true)
							.setRetrieveTables(true);
		
		final LimitOptionsBuilder limitOptionsBuilder =
				LimitOptionsBuilder.builder()
				.includeSchemas(new RegularExpressionInclusionRule("."));
//				.includeRoutineParameters(new RegularExpressionExclusionRule("."))
//				.includeSequences(new RegularExpressionExclusionRule("."))
//				.includeRoutines(new RegularExpressionExclusionRule("."));
//				.includeSynonyms(new RegularExpressionExclusionRule("^ACI_ACX_N_SSV$"));
				
				
		final LoadOptionsBuilder loadOptionsBuilder =
				LoadOptionsBuilder.builder()
				// Set what details are required in the schema - this affects the
				// time taken to crawl the schema
				.withSchemaInfoLevel(vuilder.toOptions());

		final SchemaCrawlerOptions options =
				SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
//						    		            .withLimitOptions(limitOptionsBuilder.toOptions())
				.withLoadOptions(loadOptionsBuilder.toOptions());

		Connection connection = dataSource.getConnection();

		
				final Catalog  catalog = SchemaCrawlerUtility.getCatalog(connection, options);
				Collection<schemacrawler.schema.Table> tables = catalog.getTables();
				
				return tables.parallelStream()
						.filter(t1 -> t1.getColumns().size()>0)
							.map(t -> processMetaClass(t))
							.filter(m -> m.isPresent())
							.map(m->m.get())
							.collect(Collectors.toList());
	}

	private Optional<MetaClass> processMetaClass(schemacrawler.schema.Table t) {
		MetaClass meta = new MetaClass();
		t.getForeignKeys().stream()
		.forEach(f ->{
			processForeignKey(f , meta);
		});
		meta.setTable(t);
		meta.setClassName(t.getFullName());
		String newName = processNameTranslationStrategy(t.getName());
		meta.setClassName(newName);

		meta.setTableName(t.getFullName());
		t.getColumns().stream()
		.forEach(c ->{
			MetaDataAttribute md = new MetaDataAttribute();
			Class<?> candidateClass = c.getColumnDataType().getTypeMappedClass();
			if(candidateClass.equals(BigDecimal.class)) {
				if (c.getDecimalDigits() > 0){
					md.setClazz(Double.class);
				}else {
					md.setClazz(Long.class);
				}
			}else {
				md.setClazz(c.getColumnDataType().getTypeMappedClass());
			}
			md.setColumnName(c.getName());
			md.setFieldName(processNameTranslationStrategy(c.getName()));
			Optional
				.ofNullable(c.getSize())
				.ifPresent(v -> md.setLength(String.valueOf(v)));
			md.setAttributes (c.getAttributes());
			md.setSqlType(c.getType().getDatabaseSpecificTypeName());
			if(c.isPartOfPrimaryKey() || c.isAutoIncremented()) {
				md.setId(true);
			}
			meta.addMetaAttribute(md);
		});
		return  Optional.of(meta);	
	}
	
	private void processForeignKey(ForeignKey f, MetaClass meta) {
		MetaRelationClass mrc = new MetaRelationClass(f);
		meta.addMetaRelationClass(mrc);
	}

	public String processNameTranslationStrategy(String name) {
		String newName = name.replaceAll("GRP_", "GRUPO_");
		newName  = newName .replaceAll("PRTAL$", "PORTAL");
		newName  = newName .replaceAll("PRTAL_", "PORTAL_");
		newName = newName .replaceAll("MENSG$", "MENSAGEM");
		newName = newName .replaceAll("MENSG_", "MENSAGEM_");
		newName = newName .replaceAll("USURO_", "USUARIO_");
		newName = newName .replaceAll("SITUC", "SITUACAO");
		newName = newName .replaceAll("ACEIT_", "ACEITE_");
		newName = newName .replaceAll("RGIST", "REGISTRO");
		newName = newName .replaceAll("ACSSO", "ACESSO");
		newName = newName .replaceAll("PRMRO", "PRIMEIRO");
		newName = newName .replaceAll("PESOA", "PESSOA");
		newName = newName .replaceAll("ASSES", "ASSESSORIA");
		newName = newName .replaceAll("UNIDD", "UNIDADE");
		newName = newName .replaceAll("NGOCO", "NEGOCIO");
		newName = newName .replaceAll("CRTOR", "CORRETOR");
		newName = newName .replaceAll("COMRL", "COMERCIAL");
		newName = newName .replaceAll("ENDER$", "ENDERECO");
		newName = newName .replaceAll("NAC$", "NACIONALIDADE");
		newName = newName .replaceAll("ENDER_", "ENDERECO_");
		newName = newName .replaceAll("TELEF_", "TELEFONE_");
		newName = newName .replaceAll("HISTO_", "HISTORICO_");
		newName = newName .replaceAll("ESTPL", "ESTIPULANTE");
		newName = newName .replaceAll("SISTM", "SISTEMA");
		newName = newName .replaceAll("FUCID", "FUCIONALIDADE");
		newName = newName .replaceAll("PERFL", "PERFIL");
		newName = newName .replaceAll("DIGTL", "DIGITAL");
		newName = newName .replaceAll("MOTV", "MOTIVO");
		newName = newName .replaceAll("ARQUV", "ARQUIVO");
		newName = newName .replaceAll("SISTEM_", "SISTEMA");
		newName = newName .replaceAll("OVDRIA", "OUVIDORIA");
		newName = newName .replaceAll("PARAM$", "PARAMETRO");
		newName = newName .replaceAll("PARAM_", "PARAMETRO_");
		newName = newName .replaceAll("NAC_", "NACIONALIDADE_");
		newName = newName .replaceAll("ACOMP$", "ACOMPANHAMENTO");
		newName = newName .replaceAll("VRSAO", "VERSAO");
		newName = newName .replaceAll("HISTO$", "HISTORICO");
		newName = newName .replaceAll("PRTAL$", "PORTAL");
		newName = newName .replaceAll("APLCC", "APOLICE");
		newName = newName .replaceAll("FNCLD", "FUNCIONALIDADE");
		newName = newName .replaceAll("ORIGM", "ORIGEM");
		newName = newName .replaceAll("MBILE", "MOBILE");
		newName = newName .replaceAll("PMCAO", "PROMOCAO");
		newName = newName .replaceAll("PAGNA", "PAGINA");
		newName = newName .replaceAll("CLBOR", "COLABORADOR");
		newName = newName .replaceAll("CTRAT", "CONTRATO");
		newName = newName .replaceAll("CATGO", "CATEGORIA");
		newName = newName .replaceAll("EVVDA", "ENVOLVIDA");
		newName = newName .replaceAll("TELEF_", "TELEFONE_");
		newName = newName .replaceAll("TELEF$", "TELEFONE");
		newName = newName .replaceAll("TMPLT", "TEMPLATE");
		newName = newName .replaceAll("CIDAD$", "CIDADE");
		newName = newName .replaceAll("CIDAD_", "CIDADE_");
		newName = newName .replaceAll("BAIRR$", "BAIRRO");
		newName = newName .replaceAll("BAIRR_", "BAIRRO_");
		newName = newName .replaceAll("CMPLO", "COMPLEMENTO");
		newName = newName .replaceAll("POSTL", "POSTAL");
		newName = newName .replaceAll("SUCSL", "SUCURSAL");
		newName = newName .replaceAll("GRENT", "GERENTE");
		newName = newName .replaceAll("GERNC", "GERENCIA");
		newName = newName .replaceAll("USURO", "USUARIO");
		newName = newName .replaceAll("OFCNA", "OFICINA");
		newName = newName .replaceAll("OFVSTRA", "OFICINA_VISTORIA");
		newName = newName .replaceAll("CCUST", "CENTRO_CUSTO");
		newName = newName .replaceAll("CADPAIS", "CADASTRO_PAIS");
		newName = newName .replaceAll("CADESTA", "CADASTRO_ESTADO");
		newName = newName .replaceAll("CADCID", "CADASTRO_CIDADE");
		newName = newName .replaceAll("APOLI", "APOLICE");
		newName = newName .replaceAll("IMBLR", "IMOBILIARIA");
		newName = newName .replaceAll("ASSIT", "ASSISTENCIA");
		newName = newName .replaceAll("TCNCA", "TECNICA");
		newName = newName .replaceAll("MUN_FIS", "MUNICIPIO_FISCAL");
		newName = newName .replaceAll("PAGTO", "PAGAMENTO");
		newName = newName .replaceAll("PRDUT", "PRODUTO");
		newName = newName .replaceAll("ITRNO", "INTERNO");
		newName = newName .replaceAll("FORNC", "FORNECEDOR");
		newName = newName .replaceAll("ULOGUSR", "LOG_USUARIO");
		newName = newName .replaceAll("GRENT", "GERENTE");
		newName = newName .replaceAll("ACSSO", "ACESSO");
		newName = newName .replaceAll("ULTMO", "ULTMO");
		newName = newName .replaceAll("PN$", "PARCEIRO_NEGOCIO");	
		newName = newName .replaceAll("PREDR$", "PRESTADOR");
		newName = newName .replaceAll("PRDUT", "PRODUTO");
		newName = newName .replaceAll("PROTR$", "PRESTADOR1");
		newName = newName .replaceAll("CAD_SOC", "QUADRO_SOCIETARIO");
		newName = newName .replaceAll("QUETN", "QUESTIONARIO");
		newName = newName .replaceAll("SERVC", "SERVICO");
		newName = newName .replaceAll("PRCRO", "PARCEIRO_NEGOCIO");
		newName = newName .replaceAll("RESPT", "RESPOSTA");
		newName = newName .replaceAll("ASSIT", "ASSISTENCIA");
		newName = newName .replaceAll("TCNCA", "TECNICA");
		newName = newName .replaceAll("ELAPCRT", "EMAIL_CORRETOR");
		newName = newName .replaceAll("ANALIS", "ANALISE");
		newName = newName .replaceAll("APO_DIG", "APOLICE_DIGITAL");
		newName = newName .replaceAll("DOCTO", "DOCUMENTO");
		newName = newName .replaceAll("FVRTO", "FAVORITO");
		newName = newName .replaceAll("CADTR", "CADASTRO");
		
		newName = newName .replaceAll("SBU[0-9]+\\_", "");
		newName = newName .replaceAll("PTM[0-9]+\\_", "");
		newName = newName .replaceAll("\\_[$&%.]+", "");
		return newName;
	}

	class TableImpl implements Table{

		private String name;
		
		public TableImpl(String name2) {
			this.name = name2;
		}
		
		@Override
		public Class<? extends Annotation> annotationType() {
			return Table.class;
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public String catalog() {
			return "";
		}

		@Override
		public String schema() {
			return "";
		}

		@Override
		public UniqueConstraint[] uniqueConstraints() {
			return new UniqueConstraint[0];
		}

		@Override
		public Index[] indexes() {
			return new Index[0];
		}
		
	}
	
	class EntityImpl implements Entity{

		private String name;
		
		public EntityImpl(String name2) {
			this.name = name2;
		}
		
		@Override
		public Class<? extends Annotation> annotationType() {
			// TODO Auto-generated method stub
			return Entity.class;
		}

		@Override
		public String name() {
			return name;
		}
		
	}
}

class TableImplementation implements  Table {

	private ResultSetMetaData meta;
	TableImplementation(ResultSetMetaData meta){
		this.meta = meta;
	}

	@Override
	public Class<? extends Annotation> annotationType() {
		return null;
	}
	@Override
	public String name() {
		try {
			return meta.getTableName(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	@Override
	public String catalog() {
		try {
			return meta.getCatalogName(1);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	@Override
	public String schema() {
		try {
			return meta.getSchemaName(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	@Override
	public UniqueConstraint[] uniqueConstraints() {
		return null;
	}
	@Override
	public Index[] indexes() {
		return null;
	}}
