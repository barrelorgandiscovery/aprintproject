import java.io.File

import javax.servlet.ServletException;
import org.barrelorgandiscovery.search.BookIndexing
import org.barrelorgandiscovery.xml.VirtualBookXmlIO

def app = application.app

def action = params["action"]

def search=""

String add(String search, String element) {
	if (search == null || "".equals(search)) {
		return "+" + element
	} else {
		return search + " +" + element
	}
}

if (params.search && !params.newsearch) {
	search = params.search
}

if (action != null) {
	// handle action

	if (action == "play")
	{
		try {
			String path = new URL(params.fileref).getPath();
			File bookFile = new File(path);
			def r = VirtualBookXmlIO.read(bookFile)
			def v = app.newVirtualBook(r.virtualBook, app.repository.getInstrument(r.preferredInstrumentName))
			v.play()
		}catch(Throwable t)
		{
			t.printStackTrace(System.err);
			throw new ServletException(t.message, t);
		}
	}

	if (action == "stop")
	{
		try {
			String path = new URL(params.fileref).getPath();
			File bookFile = new File(path);
			def r = VirtualBookXmlIO.read(bookFile)
			def v = app.newVirtualBook(r.virtualBook, app.repository.getInstrument(r.preferredInstrumentName))
			v.stop()
		}catch(Throwable t)
		{
			t.printStackTrace(System.err);
			throw new ServletException(t.message, t);
		}
	}
	
}


if (params.morceau)
{
	search = add(search,"" + BookIndexing.NAME_FIELD + ":\"" + params.morceau + "\"")
}
if (params.arrangeur)
{
	search = add(search,"" + BookIndexing.ARRANGER_FIELD + ":\"" + params.arrangeur + "\"")
}
if (params.instrument)
{
	search = add(search,"" + BookIndexing.INSTRUMENT_FIELD + ":\"" + params.instrument + "\"")
}




BookIndexing bi =  app.bookIndexing
System.out.println("search for :" + search)

def docs = bi.search(search)
if (docs != null)
{
	docs = docs.toList();
}

def h = html
h.html (xmlns:"http://www.w3.org/1999/xhtml",lang:"fr",'xml:lang':"fr") {

	head('') {

		// def mkp = new groovy.xml.MarkupBuilder(out)
		mkp.yieldUnescaped  '''

		<meta name="viewport" content="width=device-width; initial-scale=1.0; maximum-scale=1.0; user-scalable=0;" />
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		
		<link rel="stylesheet" href="firefox.css">
		
		<!--[if IE]>
			<link href="ie.css" type="text/css" rel="stylesheet" />
		<![endif]-->		
	
		<!--[if !IE]>
			<link media="only screen and (max-device-width: 480px)"	href="iPhone.css" type="text/css" rel="stylesheet" />
		<![endif]-->
	
	
		'''
	}


	body (id:"cadre")
	{
		table
		{
			tr{	td {h3('APrint - pilotage distant')}}
			tr
			{
				td
				{
					form
					{
						div(id:"search")
						{
								div 
								{
									b('Morceau :')
									input(type:"text", name:"morceau", value:params.morceau)
									input(type:"button", value:"+", onClick:"javascript:document.getElementById('plus').style.display='block';")
								}

								div (id:"plus",style:"font-size:smaller; padding-left:10px;display:" + (params.arrangeur != "" || params.instrument != "" ? "block" : "none") +";")
								{
									div
									{
										b('Arrangeur :')
										input(type:"text", name:"arrangeur", value:params.arrangeur)
									}
									div
									{
										b('Instrument :')
										input(type:"text", name:"instrument", value:params.instrument)
									}
								}
									
								div	
								{
									input(type:"hidden",value:search, name:"search")
									input(id:"searchbutton",type:"submit",name:"newsearch", value:"Rechercher ..")
									
								}
						}
					}
				}
			}	
		}
		hr()
	
		
		table (width:"100%"){

			if (docs != null && docs.size() > 0)
			{
				h.ul {
					docs.each {

						def i = it

						tr (id:"cadre")
{
						
							td
							{
								table 
								{
									tr 
									{
										td (id:"bt-control")
										{
										
											a(href:"?action=play&fileref=" + URLEncoder.encode(i.get(BookIndexing.FILEREF_FIELD)) + "&search="+URLEncoder.encode(search),border:"0px")
											{img(src:"play.png",border:"0px",alt:"Jouer le fichier",title:"Jouer le fichier")}
											a(href:"?action=stop&fileref=" + URLEncoder.encode(i.get(BookIndexing.FILEREF_FIELD)) + "&search="+URLEncoder.encode(search),border:"0px")
											{img(src:"stop.png",border:"0px",alt:"Arreter",title:"Arreter")}
										}
										
										td 
										{
											img(src:"carton.png",border:"0px")
										}
									}
								}
								
							}
						
							td (id:"searchresult")
							{
								a(class:"add-playlist",href:"?action=play&fileref=" + URLEncoder.encode(i.get(BookIndexing.FILEREF_FIELD)) + "&search="+URLEncoder.encode(search))
								{
									b(i.get(BookIndexing.NAME_FIELD))
									div(class:"detail")
									{
										mkp.yield  i.get(BookIndexing.INSTRUMENT_FIELD)
										mkp.yield  " - "
										b(i.get(BookIndexing.ARRANGER_FIELD))
									}
								}
							}
							
						
						}

					}
				}
			}
			else
			{
			if (docs == null && search == null )
				{
					center {b("La recherche n'a pas aboutie.")	}
				}
			
			}

		}
		

	}
}
