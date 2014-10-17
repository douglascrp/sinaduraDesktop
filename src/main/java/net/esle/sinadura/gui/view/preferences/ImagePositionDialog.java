/*
 * # Copyright 2008 zylk.net 
 * # 
 * # This file is part of Sinadura. 
 * # 
 * # Sinadura is free software: you can redistribute it and/or modify 
 * # it under the terms of the GNU General Public License as published by 
 * # the Free Software Foundation, either version 2 of the License, or 
 * # (at your option) any later version. 
 * # 
 * # Sinadura is distributed in the hope that it will be useful, 
 * # but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * # MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * # GNU General Public License for more details. 
 * # 
 * # You should have received a copy of the GNU General Public License 
 * # along with Sinadura. If not, see <http://www.gnu.org/licenses/>. [^] 
 * # 
 * # See COPYRIGHT.txt for copyright notices and details. 
 * #
 */
package net.esle.sinadura.gui.view.preferences;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import net.esle.sinadura.core.model.PdfBlankSignatureInfo;
import net.esle.sinadura.core.service.PdfService;
import net.esle.sinadura.core.util.PdfUtil;
import net.esle.sinadura.gui.events.BotonCancelarListener;
import net.esle.sinadura.gui.model.PdfBlankSignatureInfoDesktop;
import net.esle.sinadura.gui.util.ImagesUtil;
import net.esle.sinadura.gui.util.LanguageUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.itextpdf.text.pdf.PdfReader;


public class ImagePositionDialog extends Dialog {

	private static Log log = LogFactory.getLog(ImagePositionDialog.class);
	
	private Shell sShell = null;

	private Label labelPosicion;
	private Text textRuta = null;
	private Button bottonAnterior = null;
	private Button bottonSiguiente = null;
	
	private Button bottonAceptar = null;
	private Button bottonCancelar = null;

	private ImagePositionPanel imagePositionPanel = null;
	
	
	private String stampPath = null;
	private PdfBlankSignatureInfoDesktop stampPosition = null;
	private PdfBlankSignatureInfo returnPosition = null;
	private int stampPage = 1;
	private int numberOfPages = 1;
	private String documentPath = null;
	
	
	/*
	 * Diferencial entre el sistema de medición de itext y el de esta pantalla. 
	 * 
	 * Size de la pagina en itext:
	 * 595 x 842 (A4??)
	 * 
	 * Size del la imagen con pdfbox (con 52 como dpi):
	 * 430 x 608
	 * 
	 * 595 / 430 = 1.38372093
	 * 842 / 608 = 1.384868421
	 * 
	 * No se porque el ratio es distinto entre X e Y (por la conversion de pdfBox?), asi que hay que manejar dos valores.
	 * 
	 */ 
	private static final Float RELACION_X = new Float(1.38372093);
	private static final Float RELACION_Y = new Float(1.384868421);
	
	
	private int mode = MODE_ADV;
	private static int MODE_SIMPLE = 0; // sin navegacion y con imagen de fondo
	private static int MODE_ADV = 1; // con navegaacion y documento
	
	
	public ImagePositionDialog(Shell parent, String stampPath, PdfBlankSignatureInfo pdfBlankSignatureInfo) {

		this(parent, stampPath, pdfBlankSignatureInfo, null);
	}

	
	public ImagePositionDialog(Shell parent, String stampPath, PdfBlankSignatureInfo pdfBlankSignatureInfo, String documentPath) {
		
		super(parent);
		
		if (documentPath == null) {
			mode = MODE_SIMPLE;
		} else {
			mode = MODE_ADV;
		}
		
		this.stampPath = stampPath;
		this.documentPath = documentPath;
		this.stampPage = pdfBlankSignatureInfo.getPage();
		this.returnPosition = pdfBlankSignatureInfo;
		
		PdfBlankSignatureInfoDesktop pdfBlankSignatureInfoDesktop = new PdfBlankSignatureInfoDesktop();
		pdfBlankSignatureInfoDesktop.setName(pdfBlankSignatureInfo.getName());
		pdfBlankSignatureInfoDesktop.setPage(pdfBlankSignatureInfo.getPage());
		pdfBlankSignatureInfoDesktop.setStartX(Math.round(pdfBlankSignatureInfo.getStartX() / RELACION_X));
		pdfBlankSignatureInfoDesktop.setStartY(Math.round(pdfBlankSignatureInfo.getStartY() / RELACION_Y));
		pdfBlankSignatureInfoDesktop.setWidht(Math.round(pdfBlankSignatureInfo.getWidht() / RELACION_X));
		pdfBlankSignatureInfoDesktop.setHeight(Math.round(pdfBlankSignatureInfo.getHeight() / RELACION_Y));
		
		this.stampPosition = pdfBlankSignatureInfoDesktop;
		
		if (mode == MODE_ADV) {
			try {
				// password!! --> PdfReader reader = new PdfReader(inputPath, ownerPassword);
				PdfReader reader = new PdfReader(documentPath);
				numberOfPages = reader.getNumberOfPages();
				
				// TODO cerrar reader???
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	

	public PdfBlankSignatureInfo createSShell() {
		
		Shell parent = getParent();
		sShell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE );
		sShell.setImage(new Image(this.sShell.getDisplay(), Thread.currentThread().getContextClassLoader().getResourceAsStream(ImagesUtil.SINADURA_LOGO_IMG)));
		sShell.setText(LanguageUtil.getLanguage().getString("preferences.pdf.stamp.position.tittle"));
		
		sShell.setLayout(new GridLayout());
		
		if (mode == MODE_ADV) {	
			createCompositeNavigator();
		}
		
		createCompositeCampos();
		
		createCompositeBotones();		

		sShell.setSize(new Point(478, 720));
		
		// to center the shell on the screen
		Monitor primary = this.sShell.getDisplay().getPrimaryMonitor();
	    org.eclipse.swt.graphics.Rectangle bounds = primary.getBounds();
	    org.eclipse.swt.graphics.Rectangle rect = this.sShell.getBounds();
	    int x = bounds.x + (bounds.width - rect.width) / 2;
	    int y = bounds.y + (bounds.height - rect.height) / 3;
	    this.sShell.setLocation(x, y);
		
		sShell.open();
		
		Display display = parent.getDisplay();
		
		while (!sShell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		
		return returnPosition;
	}
	
	
	private void createCompositeCampos() { 
		
		Composite compositeEmail = new Composite(sShell, SWT.BORDER);
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.verticalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		compositeEmail.setLayoutData(gd);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.verticalSpacing = 5;
		gridLayout.horizontalSpacing = 50;
		compositeEmail.setLayout(gridLayout);
		
		compositeEmail.setBackground(new Color(Display.getDefault(), 255, 255, 255));
		
		
		Image backgroundImage = null;
		
		if (mode == MODE_ADV) {
			try {
				FileInputStream fis = new FileInputStream(documentPath);
				
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				PdfUtil.getPageImage(fis, baos, stampPage);
				InputStream is = new ByteArrayInputStream(baos.toByteArray());
				backgroundImage = new Image(sShell.getDisplay(), is);
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		imagePositionPanel = new ImagePositionPanel(compositeEmail, stampPath, stampPosition, backgroundImage);
	}
	
	private void createCompositeNavigator() {

		Composite compositeBotones = new Composite(sShell, SWT.NONE);
		GridData gd10 = new GridData();
		gd10.horizontalAlignment = GridData.CENTER;
		gd10.horizontalSpan = 1;
		compositeBotones.setLayoutData(gd10);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		gridLayout.horizontalSpacing = 10;
		compositeBotones.setLayout(gridLayout);

		bottonAnterior = new Button(compositeBotones, SWT.NONE);
		bottonAnterior.setText("Anterior i18n");
		bottonAnterior.addSelectionListener(new BotonAnteriorListener());

		Composite compositePagination = new Composite(compositeBotones, SWT.NONE);
		GridData gd11 = new GridData();
		gd11.horizontalAlignment = GridData.CENTER;
		gd11.horizontalSpan = 1;
		compositePagination.setLayoutData(gd11);

		GridLayout gridLayout2 = new GridLayout();
		gridLayout2.numColumns = 2;
		gridLayout2.horizontalSpacing = 3;
		compositePagination.setLayout(gridLayout2);
		
		textRuta = new Text(compositePagination, SWT.BORDER);
		textRuta.setText(String.valueOf(stampPage));
		textRuta.setEnabled(false);
		
		labelPosicion = new Label(compositePagination, SWT.NONE);
		labelPosicion.setText("/ " + numberOfPages);

		
		bottonSiguiente = new Button(compositeBotones, SWT.NONE);
		bottonSiguiente.setText("Siguiente i18n");
		bottonSiguiente.addSelectionListener(new BotonSiguienteListener());
		
		updateNavigatorButtons();
	}
	
	private void createCompositeBotones() {

		Composite compositeBotones = new Composite(sShell, SWT.NONE);
		GridData gd10 = new GridData();
		gd10.horizontalAlignment = GridData.CENTER;
		gd10.horizontalSpan = 1;
		compositeBotones.setLayoutData(gd10);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.horizontalSpacing = 30;
		compositeBotones.setLayout(gridLayout);

		bottonAceptar = new Button(compositeBotones, SWT.NONE);
		bottonAceptar.setText(LanguageUtil.getLanguage().getString("button.accept"));
		bottonAceptar.setImage(new Image(sShell.getDisplay(), Thread.currentThread().getContextClassLoader().getResourceAsStream(ImagesUtil.ACEPTAR_IMG)));
		bottonAceptar.addSelectionListener(new BotonAceptarListener());

		bottonCancelar = new Button(compositeBotones, SWT.NONE);
		bottonCancelar.setText(LanguageUtil.getLanguage().getString("button.cancel"));
		bottonCancelar.setImage(new Image(sShell.getDisplay(), Thread.currentThread().getContextClassLoader().getResourceAsStream(ImagesUtil.CANCEL_IMG)));
		bottonCancelar.addSelectionListener(new BotonCancelarListener());

	}
	
	class BotonAceptarListener implements SelectionListener {
		
		public void widgetSelected(SelectionEvent event) {
			
			// se setean los nuevos datos
			returnPosition.setStartX(stampPosition.getStartX() * RELACION_X);
			returnPosition.setStartY(stampPosition.getStartY() * RELACION_Y);
			returnPosition.setWidht(stampPosition.getWidht() * RELACION_X);
			returnPosition.setHeight(stampPosition.getHeight() * RELACION_Y);
			
			sShell.dispose();
		}

		public void widgetDefaultSelected(SelectionEvent event) {
			widgetSelected(event);			
		}
	}
	
	class BotonAnteriorListener implements SelectionListener {
		
		public void widgetSelected(SelectionEvent event) {
			
			stampPage = stampPage - 1;
			
			updateNavigatorButtons();
			
			Image backgroundImage = null;
			try {
				FileInputStream fis = new FileInputStream(documentPath);
				
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				PdfUtil.getPageImage(fis, baos, stampPage);
				InputStream is = new ByteArrayInputStream(baos.toByteArray());
				backgroundImage = new Image(sShell.getDisplay(), is);
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			imagePositionPanel.setBackgroundImage(backgroundImage);
			
			imagePositionPanel.redraw();
		}

		public void widgetDefaultSelected(SelectionEvent event) {
			widgetSelected(event);			
		}
	}

	class BotonSiguienteListener implements SelectionListener {
		
		public void widgetSelected(SelectionEvent event) {
			
			stampPage = stampPage + 1;
			
			updateNavigatorButtons();
			
			Image backgroundImage = null;
			try {
				FileInputStream fis = new FileInputStream(documentPath);
				
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				PdfUtil.getPageImage(fis, baos, stampPage);
				InputStream is = new ByteArrayInputStream(baos.toByteArray());
				backgroundImage = new Image(sShell.getDisplay(), is);
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			imagePositionPanel.setBackgroundImage(backgroundImage);
			
			imagePositionPanel.redraw();
		}
	
		public void widgetDefaultSelected(SelectionEvent event) {
			widgetSelected(event);			
		}
	}
	
	private void updateNavigatorButtons() {
		
		textRuta.setText(String.valueOf(stampPage));
		
		if (numberOfPages == 1) {
			bottonAnterior.setEnabled(false);
			bottonSiguiente.setEnabled(false);
		} else if (stampPage == 1) {
			bottonAnterior.setEnabled(false);
			bottonSiguiente.setEnabled(true);
		} else if (stampPage == numberOfPages) {
			bottonAnterior.setEnabled(true);
			bottonSiguiente.setEnabled(false);
		} else {
			bottonAnterior.setEnabled(true);
			bottonSiguiente.setEnabled(true);
		}
	}

}