/**
 * 
 */
package classes;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author chris
 *
 */
@XmlRootElement
public class Iq {
	@XmlAttribute(name="id")
	public String id;
}
