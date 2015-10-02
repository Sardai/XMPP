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


@XmlRootElement(name="stream")
public class Stream {
	@XmlAttribute(name="to")
	public String to;
	@XmlAttribute(name="from")
	public String from;
	@XmlAttribute(name="id")
	public String id;
}
