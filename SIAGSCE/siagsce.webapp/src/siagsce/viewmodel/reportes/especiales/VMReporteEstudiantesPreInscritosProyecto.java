package siagsce.viewmodel.reportes.especiales;


import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Popup;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabs;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;


import siagsce.herramientas.MensajesEmergentes;
import siagsce.modelo.data.maestros.CoordinadorSce;
import siagsce.modelo.data.maestros.DireccionPrograma;
import siagsce.modelo.data.maestros.DirectorPrograma;
import siagsce.modelo.data.maestros.Profesor;
import siagsce.modelo.data.maestros.Proyecto;
import siagsce.modelo.data.maestros.RepresentanteProfesoral;
import siagsce.modelo.data.reportes.abiertos.ListaEstudiante;
import siagsce.modelo.general.StatusCoordinadorSce;
import siagsce.modelo.general.StatusDirectorPrograma;
import siagsce.modelo.general.StatusRepresentanteProfesoral;
import siagsce.modelo.servicio.maestros.SActividad;
import siagsce.modelo.servicio.maestros.SCoordinadorSce;
import siagsce.modelo.servicio.maestros.SDireccionPrograma;
import siagsce.modelo.servicio.maestros.SDirectorPrograma;
import siagsce.modelo.servicio.maestros.SProfesor;
import siagsce.modelo.servicio.maestros.SProyecto;
import siagsce.modelo.servicio.maestros.SRepresentanteProfesoral;
import siagsce.modelo.servicio.reportes.abiertos.SListadoEstudiante;
import siagsce.viewmodel.reportes.abiertos.ReportConfig;
import siagsce.viewmodel.reportes.abiertos.ReportType;
import siagsce.viewmodel.seguridad.SecurityUtil;

/**
 * ViewModel para la vista del reporte de  Consulta de Estudiantes Pre-Incritos por Proyecto,
 * Filtrado por Direcci�n de Programa, Proyecto.
 * @author Iterator
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class VMReporteEstudiantesPreInscritosProyecto {

	/** 
	 * Declaraci�n de Variables del ViewModel
	 * */
	private String cedula;
	private String nombre;
	private String apellido;
	private String email;
	private String telefono;
	private String direccionNombre;
	private Date fechaInicio;
	private Date fechaFin;
	MensajesEmergentes mensajeEmergente = new MensajesEmergentes();
	SimpleDateFormat format;
	/**
	 * ESTO PARA FORMATO DE FECHA Y PARA OBTENER LA DEL SISTEMA
	 */
	Date fechaActual = new Date();
	
	/** 
	 * Declaraci�n de Componentes de la vista
	 * */
	@Wire
	private Button Exportar;
	@Wire
	private Button btnCancelarConsutaEstudianteH;
	@Wire
	private Textbox txtFiltroEstudiantesPreInscritos;
	@Wire 
	Combobox cmbPrograma;
	@Wire 
	Combobox cmbProyectos;
	@Wire 
	Popup popOpciones;
	@Wire
	private Datebox dtbfechaFin;
	@Wire
	private Datebox dtbfechaIni;
	private Window win;
	/** 
	 * Declaraci�n de listas y otras estructuras de datos
	 * */
	List<ListaEstudiante> listestudiante= new ArrayList<ListaEstudiante>();
	private List<String> valores;
	private String seleccion;
	private String nombrefiltro;	

	ReportType reportType = null;
	private ReportConfig reportConfig = null;
	private String ruta="/WEB-INF/siagsce/reportes/especiales/ReporteEstudiantesPreincritosProyecto.jasper";
	private String proyecto_param;

	/**
	 * Lista que permite llenar el combo para elegir el formato 
	 * 
	 **/	
	private ListModelList<ReportType> reportTypesModel = new ListModelList<ReportType>(
			Arrays.asList( 
					new ReportType("Word (RTF)", "rtf"), 
					new ReportType("CSV", "csv"), 
					new ReportType("OpenOffice (ODT)", "odt")));

	/**
	 * Declaraci�n de los servicios
	 * 
	 **/
	@WireVariable
	SDireccionPrograma sdireccionPrograma;
	@WireVariable
	SRepresentanteProfesoral smiembroCoordinacion ;
	@WireVariable
	SDirectorPrograma sdirector ;
	@WireVariable
	SProfesor sprofesor ;
	@WireVariable
	SActividad sactividad;
	@WireVariable
	SProyecto sproyecto ;
	@WireVariable
	SCoordinadorSce scoordinacorSce;
	@WireVariable
	SListadoEstudiante sListadoEstudiantesDireccionPrograma;


	/**
	 * Declaraci�n de los modelos a ser utilizados en los listados y combos
	 **/	
	private ListModelList<DireccionPrograma> modeloPrograma;
	private List<DireccionPrograma> programas;
	private ListModelList<ListaEstudiante> modeloEstudianteDireccionPrograma;

	private ListModelList<Proyecto> modeloProyecto;
	private List<Proyecto> proyecto;

	/**
	 * Declaraci�n de variables donde sera guardada las selecciones
	 **/
	private DireccionPrograma selectedPrograma;
	private Proyecto selectedProyecto;




	/**
	 * M�todo que inicializa los modelos,carga el programa y el proyecto desde la BD 
	 * y llena los combos respectivamente
	 * @param win ventana la cual esta asociada a este viewmodel
	 * 
	 */
	@Init
	public void init(@ContextParam(ContextType.COMPONENT) Component win) {
		this.win = (Window) win;
		programas=buscarDireccionesXRol();
		modeloPrograma = new ListModelList<DireccionPrograma>(programas);	
		proyecto =sproyecto.buscarTodo();
		modeloProyecto = new ListModelList<Proyecto>();
		modeloEstudianteDireccionPrograma = new ListModelList<ListaEstudiante>();
		valores = new ArrayList<String>();
		valores.add("C�dula");
		valores.add("Nombre");
		valores.add("Apellido");
	}
	/**
	 * M�todo que aplica la seguridad funcional en el reporte
	 * para buscar las direcciones de programa dado el rol que posea el usuario.
	 * @return lista de direccion de programa
	 **/
	

	public List<DireccionPrograma> buscarDireccionesXRol(){
		List<DireccionPrograma>programas=new ArrayList<DireccionPrograma>();
		String cedula= SecurityUtil.nombreUsuario;
		Profesor profesor=sprofesor.buscarPorCedula(cedula);
		if(SecurityUtil.verificarRol("Coordinacion SCE")){
			programas=sdireccionPrograma.buscarTodo();
		}
		else{
			if(SecurityUtil.verificarRol("Director")){
				DirectorPrograma director= sdirector.buscarPorProfesorYEstatus(profesor, StatusDirectorPrograma.Activo.toString());
				programas.add(director.getDireccionPrograma());
			}
				
			if(SecurityUtil.verificarRol("Representante Profesoral")){
				RepresentanteProfesoral repre=smiembroCoordinacion.buscarRepresentantePorCedula(profesor, StatusRepresentanteProfesoral.Activo.toString());
				programas.add(repre.getDireccionProgramam());
				}
			if(SecurityUtil.verificarRol("Responsable Proyecto")){
				Profesor resp=sproyecto.buscarResponsableExit(profesor);
				List<Proyecto>proyectoR=sproyecto.buscarProyectosdelProfesorResponsable(resp);
				for(Proyecto proyecto:proyectoR){
					programas.addAll(proyecto.getDireccionPrograma());
				}
			}
			if(SecurityUtil.verificarRol("Participante Actividad")){
				Profesor part=sactividad.buscarParticipanteEx(profesor);
				List<Proyecto>proyectoP=sactividad.buscarProyectosDeUnProfesorParticipante(part);
				for(Proyecto proyectoA:proyectoP){
					programas.addAll(proyectoA.getDireccionPrograma());
				}
			}
		} 
		programas=eliminarRepetidos(programas);
		return programas;
	}
	/**
	 * elimina los repetidos de la lista de direcciones de programa
	 * @param lista a la cual se le eliminara las duplicidades
	 * @return lista filtrada 
	 * */
	public List<DireccionPrograma>eliminarRepetidos(List<DireccionPrograma>aux){
		List<DireccionPrograma>auxprogramas=new ArrayList<DireccionPrograma>();
		for(DireccionPrograma dir:aux){
			if(!(auxprogramas.contains(dir))){
				auxprogramas.add(dir);
			}
		}
		return auxprogramas;
	}



	/**
	 * M�todo que llena los datos de forma din�mica
	 **/	
	@Command("llenarListaDinamica")
	@NotifyChange({"selectedPrograma","selectedProyecto"})
	public void llenarListaDinamica() {
		modeloEstudianteDireccionPrograma.clear();
		String condicion=" where e.estudiante_cedula = pro.estudiante_cedula";
		/**
		 * Guarda la Selecci�n que haga el usuario en la variable codigoDireccionPrograma
		 **/
		if(selectedPrograma!=null){
			condicion=condicion+" and e.direccion_codigo="+ "'"+Integer.toString(selectedPrograma.getDireccionCodigo())+"'"+" and pp.direccion_codigo="+ "'"+Integer.toString(selectedPrograma.getDireccionCodigo())+"'";		}
		desbloquear();
		/**
		 * Guarda la Selecci�n que haga el usuario del combo proyecto
		 **/
		if(selectedProyecto!=null){
			condicion=condicion+" and pro.proyecto_codigo  ="+ "'"+selectedProyecto.getProyectoCodigo()+"'"+" and pp.proyecto_codigo  ="+ "'"+selectedProyecto.getProyectoCodigo()+"'";
			proyecto_param= selectedProyecto.getProyectoNombre();
			desbloquear();
		}
        
		/**
		 * Guarda la Selecci�n en las variables fechaInicio y fechaFin
		 */
		if(selectedProyecto!=null && fechaInicio!=null && fechaFin!=null)
		{
			if(fechaFin.after(fechaInicio)){
				condicion=condicion+" and pro.preinscripcion_proyecto_fecha BETWEEN "+"'"+fechaInicio+"'"+" and "+"'"+fechaFin+"'";
				desbloquear();
			}
			else
			{
				MensajesEmergentes mensajeEmergente = new MensajesEmergentes();
				mensajeEmergente.advertenciaValorFechas();
			}

		}

		listestudiante = sListadoEstudiantesDireccionPrograma.buscarEstudiantesPreinscritosProyectoDinamico(condicion);
		if(listestudiante.size()!=0){
			modeloEstudianteDireccionPrograma.addAll(listestudiante);
			win.getFellow("tab").getFellow("tabs").getFellow("pestana2").setVisible(false);
			Tab tab=(Tab) win.getFellow("tab").getFellow("tabs").getFellow("pestana1");
			tab.setSelected(true);
			System.out.println(condicion);
		}else{
			Exportar.setDisabled(true);
		}
		
	}

	/**
	 * M�todo que carga el reporte
	 **/	
	@Command("showReport")
	@NotifyChange({"reportConfig","prueba"})
	public void showReport() {
		MensajesEmergentes mensajeEmergente = new MensajesEmergentes();
		if(selectedPrograma==null && selectedProyecto==null){
			mensajeEmergente.advertenciaCriterioBusqueda();
		}else{
			reportConfig = new ReportConfig(ruta);
			reportConfig.getParameters().put("programa", selectedPrograma.getDireccionNombre());
			reportConfig.setType(reportType);
			reportConfig.setDataSource(new JRBeanCollectionDataSource(modeloEstudianteDireccionPrograma));
			Exportar.setDisabled(true);
		}
	}

	/**				
	 * M�todo que Carga en el combo de proyecto 
	 * de acuerdo al programa que se seleccione			
	 **/
	@NotifyChange({ "modeloProyecto" })
	@Command
	public void cargarProyecto() {
		selectedProyecto=null;
		fechaFin=null;
		fechaInicio=null;
		obtenerProyectosXProgramaYProfesor();
		llenarListaDinamica();
	}
	@Command
	public void obtenerProyectosXProgramaYProfesor(){
		modeloProyecto.clear();
		List<Proyecto>proyectos=new ArrayList<Proyecto>();
		String cedula= SecurityUtil.nombreUsuario;
		Profesor profesor=sprofesor.buscarPorCedula(cedula);
		RepresentanteProfesoral repre=smiembroCoordinacion.buscarRepresentantePorProgramaEstatus(profesor, selectedPrograma, StatusRepresentanteProfesoral.Activo.toString());
		DirectorPrograma director= sdirector.buscarDirectorProgramaEstatus(profesor, selectedPrograma, StatusDirectorPrograma.Activo.toString());
		CoordinadorSce coord= scoordinacorSce.buscarPorProfesorYEstatus(profesor, StatusCoordinadorSce.Activo.toString());
		Profesor resp=sproyecto.buscarResponsableExit(profesor);
		Profesor part=sactividad.buscarParticipanteEx(profesor);
		if(coord!=null||repre!=null||director!=null){
			System.out.println("cualquiera");
			proyectos=sproyecto.buscarProyectoNoInactivosPrograma(selectedPrograma);
			System.out.println("cualquiera2");
		}
		else{
			if(resp!=null){
				System.out.println("responsable");
				List<Proyecto>proyectoR=sproyecto.buscarProyectoResponsablePrograma(resp, selectedPrograma);
				proyectos.addAll(proyectoR);
				System.out.println("responsable2");
				
			}
			if(part!=null){
				List<Proyecto>proyectoP=sactividad.findProyectosNoInactivosDeUnProfesorParticipante(part);
				proyectos.addAll(proyectoP);
			}
		}
		
		 proyectos=eliminarRepetidosProyecto(proyectos);
		 modeloProyecto.addAll(proyectos);
	}


	public List<Proyecto>eliminarRepetidosProyecto(List<Proyecto>aux){
		List<Proyecto>auxproyecto=new ArrayList<Proyecto>();
		for(Proyecto proyecto:aux){
			if(!(auxproyecto.contains(proyecto)))
				auxproyecto.add(proyecto);
		}
		return auxproyecto;
	}
	/**
	 * M�todo que cierra la ventana
	 */
	@Command
	public void cerrar() {
		win.detach();
	}

	/**
	 * M�todo que limpia cada combo en el .zul
	 **/				
	@Command
	public void limpiarPrograma() {
		modeloPrograma.clear();
	}
	@Command
	public void limpiarProyecto() {
		modeloProyecto.clear();
	}
	@Command
	public void limpiarmodelo() {
		modeloEstudianteDireccionPrograma.clear();
	}

	/**
	 * M�todo que cancela el proceso
	 **/		
	@Command
	@NotifyChange({ "direccionNombre", "proyectoNombre",
		"cedula", "nombre", "apellido",
		"email","telefono"})		
	public void cancelar() {
		cedula ="";
		nombre="";
		apellido ="";
		email="";
		telefono="";
		limpiarmodelo();
		cmbPrograma.setText("");
		if(cmbProyectos.getSelectedItem()!=null){
			cmbProyectos.setText("");
		}
		if(dtbfechaIni.getText()!=""){
			dtbfechaIni.setText("");
		}
		if(dtbfechaFin.getText()!=""){
			dtbfechaFin.setText("");
		}
		win.getFellow("tab").getFellow("tabs").getFellow("pestana1").setVisible(true);
		Tab tab=(Tab) win.getFellow("tab").getFellow("tabs").getFellow("pestana1");
		tab.setSelected(true);
		Exportar.setDisabled(true);
	}

	/**
	 * Vincula elementos de la interfaz gr�fica con este ViewModel,
	 * inhabilita los botones exportar y cancelar,
	 * coloca al inicio marca de agua en el filtro nombre
	 * @param view la vista cuyos elementos se van a vincular a este ViewModel
	 */			
	@AfterCompose
	public void AfterCompose(@ContextParam(ContextType.VIEW) Component win) {
		Selectors.wireComponents(win, this, false);
		Exportar.setDisabled(true); 
		btnCancelarConsutaEstudianteH.setDisabled(true);
		txtFiltroEstudiantesPreInscritos.setPlaceholder("Nombre");
	}

	/**
	 * M�todo que desbloquea los botones
	 **/
	@Command
	public void desbloquear() {
		Exportar.setDisabled(false);
		btnCancelarConsutaEstudianteH.setDisabled(false);
	}
	/**
	 * M�todos para el filtro
	 **/
	@Command
	@NotifyChange({ "modeloEstudianteDireccionPrograma" })
	public void filtrarEstudiantePreInscritos() {
		try {
			List<ListaEstudiante> aux = new ArrayList<ListaEstudiante>();
			aux = listestudiante;
			modeloEstudianteDireccionPrograma.clear();

			if (seleccion == null || seleccion == "") {
				if (nombrefiltro == "")
					modeloEstudianteDireccionPrograma.addAll(aux);
				else {
					for (int i = 0; i < aux.size(); i++) {
						if (aux.get(i).getNombre().toLowerCase()
								.contains(nombrefiltro.toLowerCase())) {
							modeloEstudianteDireccionPrograma.add(aux.get(i));

						}
					}
				}
			} else {
				if (seleccion == "C�dula") {
					if (nombrefiltro == "")
						modeloEstudianteDireccionPrograma.addAll(aux);
					else {
						for (int i = 0; i < aux.size(); i++) {
							if (aux.get(i).getCedula().toLowerCase()
									.contains(nombrefiltro.toLowerCase())) {
								modeloEstudianteDireccionPrograma.add(aux.get(i));

							}
						}
					}
				} else {
					if (seleccion == "Nombre") {
						if (nombrefiltro == "")
							modeloEstudianteDireccionPrograma.addAll(aux);
						else {
							for (int i = 0; i < aux.size(); i++) {
								if (aux.get(i).getNombre().toLowerCase()
										.contains(nombrefiltro.toLowerCase())) {
									modeloEstudianteDireccionPrograma.add(aux.get(i));

								}
							}
						}
					} else {
						if (seleccion == "Apellido") {
							if (nombrefiltro == "")
								modeloEstudianteDireccionPrograma.addAll(aux);
							else {
								for (int i = 0; i < aux.size(); i++) {
									if (aux.get(i).getApellido()
											.toLowerCase().contains(nombrefiltro.toLowerCase())) {
										modeloEstudianteDireccionPrograma.add(aux.get(i));

									}
								}
							}
						}
					}
				}
			}
		} catch (NullPointerException e) {

		}
	}
	@Command
	public void seleccionFiltro() {
		if (seleccion == "Nombre") {
			txtFiltroEstudiantesPreInscritos.setPlaceholder("Nombre");
			popOpciones.close();
		} else {
			if (seleccion == "C�dula") {
				txtFiltroEstudiantesPreInscritos.setPlaceholder("C�dula");
				popOpciones.close();
			} else {
				if (seleccion == "Apellido")
				 txtFiltroEstudiantesPreInscritos.setPlaceholder("Apellido");
					popOpciones.close();
			}
		}

	}
	
	/**
	 * M�todos set y get
	 **/
	public ListModelList<ListaEstudiante> getModeloEstudianteDireccionPrograma() {
		return modeloEstudianteDireccionPrograma;
	}


	public List<DireccionPrograma> getProgramas() {
		return programas;
	}

	public void setProgramas(List<DireccionPrograma> programas) {
		this.programas = programas;
	}

	public List<Proyecto> getProyecto() {
		return proyecto;
	}


	public void setProyecto(List<Proyecto> proyecto) {
		this.proyecto = proyecto;
	}


	public Date getFechaInicio() {
		return fechaInicio;
	}


	public void setFechaInicio(Date fechaInicio) {
		this.fechaInicio = fechaInicio;
	}


	public Date getFechaFin() {
		return fechaFin;
	}


	public void setFechaFin(Date fechaFin) {
		this.fechaFin = fechaFin;
	}

	public Combobox getCmbPrograma() {
		return cmbPrograma;
	}


	public void setCmbPrograma(Combobox cmbPrograma) {
		this.cmbPrograma = cmbPrograma;
	}


	public Combobox getCmbProyectos() {
		return cmbProyectos;
	}


	public void setCmbProyectos(Combobox cmbProyectos) {
		this.cmbProyectos = cmbProyectos;
	}


	public Popup getPopOpciones() {
		return popOpciones;
	}


	public void setPopOpciones(Popup popOpciones) {
		this.popOpciones = popOpciones;
	}


	public Datebox getDtbfechaFin() {
		return dtbfechaFin;
	}


	public void setDtbfechaFin(Datebox dtbfechaFin) {
		this.dtbfechaFin = dtbfechaFin;
	}


	public Datebox getDtbfechaIni() {
		return dtbfechaIni;
	}


	public void setDtbfechaIni(Datebox dtbfechaIni) {
		this.dtbfechaIni = dtbfechaIni;
	}


	public List<ListaEstudiante> getListestudiante() {
		return listestudiante;
	}


	public void setListestudiante(List<ListaEstudiante> listestudiante) {
		this.listestudiante = listestudiante;
	}


	public List<String> getValores() {
		return valores;
	}


	public void setValores(List<String> valores) {
		this.valores = valores;
	}


	public String getSeleccion() {
		return seleccion;
	}


	public void setSeleccion(String seleccion) {
		this.seleccion = seleccion;
	}


	public String getNombrefiltro() {
		return nombrefiltro;
	}


	public void setNombrefiltro(String nombrefiltro) {
		this.nombrefiltro = nombrefiltro;
	}

	public Textbox getTxtFiltroEstudiantesPreInscritos() {
		return txtFiltroEstudiantesPreInscritos;
	}


	public void setTxtFiltroEstudiantesPreInscritos(
			Textbox txtFiltroEstudiantesPreInscritos) {
		this.txtFiltroEstudiantesPreInscritos = txtFiltroEstudiantesPreInscritos;
	}


	/**
	 * M�todos set y get que permite manipular la vista .zul
	 **/
	public ListModelList<ReportType> getReportTypesModel() {
		return reportTypesModel;
	}

	public ReportConfig getReportConfig() {
		return reportConfig;
	}

	public ReportType getReportType() {
		return reportType;
	}

	public void setReportType(ReportType reportType) {
		this.reportType = reportType;
	}

	public Proyecto getSelectedProyecto() {
		return selectedProyecto;
	}

	public void setSelectedProyecto(Proyecto selectedProyecto) {
		this.selectedProyecto = selectedProyecto;
	}
	public ListModelList<DireccionPrograma> getModeloPrograma() {
		return modeloPrograma;
	}

	public void setModeloPrograma(ListModelList<DireccionPrograma> modeloPrograma) {
		this.modeloPrograma = modeloPrograma;
	}

	public void setModeloEstudianteDireccionPrograma(
			ListModelList<ListaEstudiante> modeloEstudianteDireccionPrograma) {
		this.modeloEstudianteDireccionPrograma = modeloEstudianteDireccionPrograma;
	}

	public DireccionPrograma getSelectedPrograma() {
		return selectedPrograma;
	}

	public void setSelectedPrograma(DireccionPrograma selectedPrograma) {
		this.selectedPrograma = selectedPrograma;
	}


	public ListModelList<Proyecto> getModeloProyecto() {
		return modeloProyecto;
	}


	public void setModeloProyecto(ListModelList<Proyecto> modeloProyecto) {
		this.modeloProyecto = modeloProyecto;
	}

}
